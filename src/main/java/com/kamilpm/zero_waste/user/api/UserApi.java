package com.kamilpm.zero_waste.user.api;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import com.kamilpm.zero_waste.common.dto.NotificationRecipient;
import com.kamilpm.zero_waste.common.dto.UserAuthenticationData;
import com.kamilpm.zero_waste.common.dto.UserRole;
import com.kamilpm.zero_waste.common.dto.UserSummaryDto;
import com.kamilpm.zero_waste.common.dto.UserSummaryWithEmailDto;
import com.kamilpm.zero_waste.common.events.BanEvent;
import com.kamilpm.zero_waste.common.events.RevokeRefreshTokenEvent;
import com.kamilpm.zero_waste.common.events.SendBanNotificationEvent;
import com.kamilpm.zero_waste.common.events.UnbanEvent;
import com.kamilpm.zero_waste.common.exception.ConflictException;
import com.kamilpm.zero_waste.common.exception.EntityNotFoundException;
import com.kamilpm.zero_waste.common.exception.ForbiddenException;
import com.kamilpm.zero_waste.common.interfaces.UserProvider;
import com.kamilpm.zero_waste.user.entity.User;
import com.kamilpm.zero_waste.user.entity.UserBan;
import com.kamilpm.zero_waste.user.mapper.UserMapper;
import com.kamilpm.zero_waste.user.repository.UserBanRepository;
import com.kamilpm.zero_waste.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserApi implements UserProvider {
  private final UserRepository userRepository;
  private final UserBanRepository userBanRepository;
  private final UserMapper userMapper;
  private final ApplicationEventPublisher events;

  @Value("${app.security.demo.email}")
  private String demoEmail;

  @Override
  public UserAuthenticationData findAuthenticationData(String email) {
    User user = userRepository.findByEmail(email).orElseThrow(() -> new EntityNotFoundException("User not found"));

    clearExpiredBan(user);

    return userMapper.toUserAuthenticationData(user);

  }

  private void clearExpiredBan(User user) {
    if (!user.isBanActive()) {
      return;
    }
    if (user.getBannedUntil() == null) {
      return;
    }
    if (user.getBannedUntil().isAfter(Instant.now()))
      return;

    userBanRepository.findTopByUserIdAndRevokedAtIsNullOrderByCreatedAtDesc(user.getId()).ifPresent(ban -> {
      ban.setRevokedAt(Instant.now());
      ban.setRevokedReason("Expired");
    });

    user.setBanActive(false);
    user.setBannedUntil(null);
    events.publishEvent(new UnbanEvent(List.of(user.getId())));
  }

  @Override
  public Set<UUID> findExcludedUserIdsForPublicContent() {
    return userRepository.findIdsByBanActiveTrueOrRole(UserRole.DEMO);
  }

  @Override
  public void savePassword(UUID userId, String passwordHash) {
    userRepository.updatePassword(userId, passwordHash);
  }

  @Override
  public UserAuthenticationData getDemoUser() {
    User user = userRepository.findByEmail(demoEmail)
        .orElseThrow(() -> new EntityNotFoundException("Demo user not found"));
    return userMapper.toUserAuthenticationData(user);

  }

  @Override
  public UserAuthenticationData findById(UUID id) {
    User user = userRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("User not found"));
    return userMapper.toUserAuthenticationData(user);
  }

  @Override
  public UserSummaryDto findUserSummaryById(UUID id) {
    User user = userRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("User not found"));
    return userMapper.toUserSummaryDto(user);
  }

  @Override
  public Optional<UserAuthenticationData> findAuthenticatedUserByEmail(String email) {
    return userRepository.findByEmail(email).map(userMapper::toUserAuthenticationData);
  }

  @Override
  public UserAuthenticationData createOAuthUser(String email, String nickname) {
    User user = User.builder()
        .nickname(nickname)
        .email(email.toLowerCase())
        .password(null)
        .role(UserRole.USER)
        .banActive(false)
        .bannedUntil(null)
        .build();
    return userMapper.toUserAuthenticationData(userRepository.save(user));
  }

  @Override
  public boolean isUserDemo(UUID userId) {
    return userRepository.findById(userId).map(user -> user.getRole() == UserRole.DEMO).orElse(false);
  }

  @Override
  public Map<UUID, UserAuthenticationData> getUsersByIds(Collection<UUID> ids) {
    return userRepository.findAllById(ids).stream()
        .collect(Collectors.toMap((user) -> user.getId(), userMapper::toUserAuthenticationData));

  }

  @Override
  public Map<UUID, UserSummaryDto> getUserSummaryByIds(Collection<UUID> ids) {
    return userRepository.findAllById(ids).stream()
        .collect(Collectors.toMap((user) -> user.getId(), userMapper::toUserSummaryDto));
  }

  @Override
  public String getUserEmail(UUID userId) {
    return userRepository.findById(userId).map(user -> user.getEmail())
        .orElseThrow(() -> new EntityNotFoundException("Buyer not found"));
  }

  @Override
  public Map<UUID, UserSummaryWithEmailDto> getUserSummaryWithEmailByIds(Collection<UUID> ids) {
    return userRepository.findAllById(ids).stream()
        .collect(Collectors.toMap((user) -> user.getId(), userMapper::toUserSummaryWithEmailDto));
  }

  @Override
  public List<NotificationRecipient> getUsersEmail(List<UUID> userIds) {
    return userRepository.findAllById(userIds).stream()
        .map(user -> new NotificationRecipient(user.getId(),
            user.getEmail()))
        .toList();
  }

  @Override
  public Set<UUID> findExcludedAuthorIdsForPublicContent() {
    return userRepository.findIdsByBanActiveTrueAndRoleWriterOrRoleDemo();
  }

  public void userExists(UUID subjectId, UUID userId) {
    User reportedUser = userRepository.findById(subjectId)
        .orElseThrow(() -> new EntityNotFoundException("User not found"));

    if (Objects.equals(reportedUser.getId(), userId))
      throw new ForbiddenException("You can not report yourself");

    if (Objects.equals(reportedUser.getRole(), UserRole.DEMO))
      throw new ForbiddenException("Unable to interact with demo users");

    if (reportedUser.isBanActive())
      throw new ForbiddenException("Unable to report banned user");

  }

  public void banUser(UUID adminId, UUID reportId, String adminNote) {

    User userToBan = userRepository.findById(reportId)
        .orElseThrow(() -> new EntityNotFoundException("User not found"));

    if (userToBan.isBanActive() && userToBan.getBannedUntil() == null) {
      throw new ConflictException("User already banned");
    }

    userToBan.setBanActive(true);
    userToBan.setBannedUntil(null);

    UserBan userBan = UserBan.builder()
        .userId(userToBan.getId())
        .reason(adminNote)
        .createdAt(Instant.now())
        .bannedBy(adminId)
        .expiresAt(null)
        .build();
    userRepository.save(userToBan);
    userBanRepository.save(userBan);

    events.publishEvent(new RevokeRefreshTokenEvent(List.of(userToBan.getId())));
    events.publishEvent(new SendBanNotificationEvent(userToBan.getEmail()));
    events.publishEvent(new BanEvent(List.of(userToBan.getId())));

  }
}
