package com.kamilpm.zero_waste.user.service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kamilpm.zero_waste.auth.api.CurrentUserApi;
import com.kamilpm.zero_waste.auth.api.RevokeRefreshTokenEvent;
import com.kamilpm.zero_waste.common.exception.ConflictException;
import com.kamilpm.zero_waste.common.exception.EntityNotFoundException;
import com.kamilpm.zero_waste.common.exception.ForbiddenException;
import com.kamilpm.zero_waste.common.utils.OwnMapper;
import com.kamilpm.zero_waste.common.utils.SqlUtils;
import com.kamilpm.zero_waste.user.dto.AuthenticatedUser;
import com.kamilpm.zero_waste.user.dto.BanRequest;
import com.kamilpm.zero_waste.user.dto.CreateUserRequest;
import com.kamilpm.zero_waste.user.dto.UnbanRequest;
import com.kamilpm.zero_waste.user.dto.UpdateUserRequest;
import com.kamilpm.zero_waste.user.dto.UserDto;
import com.kamilpm.zero_waste.user.dto.UserRole;
import com.kamilpm.zero_waste.user.entity.User;
import com.kamilpm.zero_waste.user.entity.UserBan;
import com.kamilpm.zero_waste.user.mapper.UserMapper;
import com.kamilpm.zero_waste.user.repository.UserBanRepository;
import com.kamilpm.zero_waste.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {

  private final UserRepository userRepository;
  private final CurrentUserApi currentUser;
  private final PasswordEncoder passwordEncoder;
  private final UserBanRepository userBanRepository;
  private final UserMapper userMapper;
  private final SimpMessagingTemplate simpMessagingTemplate;
  private final ApplicationEventPublisher events;

  @Transactional(readOnly = true)
  public Page<UserDto> getUsersWithoutCurrentUser(String text, List<UserRole> roles, Pageable pageable) {

    if (roles != null && roles.isEmpty())
      roles = null;
    text = SqlUtils.prepareLikePattern(text);
    AuthenticatedUser user = getRequiredAuthenticatedUser();
    return userRepository.findAllByIdNot(user.id(), text, roles, pageable).map(userMapper::toDto);
  }

  @Transactional
  public UserDto createUser(CreateUserRequest userRequest) {

    if (userRepository.existsByEmail(userRequest.getEmail())) {
      throw new ConflictException("Email already in use", "email");
    }

    final User user = User.builder()
        .nickname(userRequest.getNickname())
        .email(userRequest.getEmail().toLowerCase())
        .password(passwordEncoder.encode(userRequest.getPassword()))
        .role(userRequest.getRole())
        .banActive(false)
        .bannedUntil(null)
        .build();

    User savedUser = userRepository.save(user);
    return userMapper.toDto(savedUser);
  }

  @Transactional(readOnly = true)
  public UserDto getUser(final UUID id) {
    User user = findUser(id);
    if (user.isBanActive()) {
      throw new EntityNotFoundException("User not found");
    }

    return userMapper.toDto(user);
  }

  private User findUser(UUID id) {
    return userRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("User not found"));
  }

  @Transactional
  public UserDto updateUser(final UUID id, final UpdateUserRequest userRequest) {

    AuthenticatedUser admin = getRequiredAuthenticatedUser();

    if (Objects.equals(admin.id(), id)) {
      throw new ForbiddenException("You can not update your account");
    }

    if (userRepository.existsByEmailAndIdNot(userRequest.getEmail(), id)) {
      throw new ConflictException("Email already in use", "email");
    }

    final User user = findUser(id);

    user.setNickname(userRequest.getNickname());
    user.setEmail(userRequest.getEmail());
    user.setRole(userRequest.getRole());
    if (userRequest.getEmail() != null && !userRequest.getPassword().isBlank()) {
      user.setPassword(passwordEncoder.encode(userRequest.getPassword()));
    }
    User updatedUser = userRepository.save(user);
    return userMapper.toDto(updatedUser);
  }

  @Transactional
  public void deleteUser(final List<UUID> ids) {

    AuthenticatedUser admin = getRequiredAuthenticatedUser();

    if (ids.stream().anyMatch((id) -> Objects.equals(admin.id(), id))) {
      throw new ForbiddenException("You can not delete your account");
    }
    deleteUsersByIds(ids);

  }

  @Transactional
  public void banUsers(final BanRequest banRequest) {
    AuthenticatedUser admin = getRequiredAuthenticatedUser();

    List<User> users = userRepository.findAllById(banRequest.getIds());

    List<UserBan> bans = new ArrayList<>();
    List<UUID> bannedUserIds = new ArrayList<>();
    Instant now = Instant.now();

    for (final User user : users) {
      if (Objects.equals(admin.id(), user.getId())) {
        continue;
      }

      if (user.isBanActive()) {
        continue;
      }
      user.setBanActive(true);
      user.setBannedUntil(banRequest.getExpiresAt());
      bannedUserIds.add(user.getId());

      bans.add(
          UserBan.builder()
              .userId(user.getId())
              .reason(banRequest.getReason())
              .createdAt(now)
              .bannedBy(admin.id())
              .expiresAt(banRequest.getExpiresAt())
              .build());
    }

    events.publishEvent(new RevokeRefreshTokenEvent(bannedUserIds));
    userBanRepository.saveAll(bans);
    userRepository.saveAll(users);

    for (User user : users) {
      simpMessagingTemplate.convertAndSendToUser(user.getEmail(),
          "/queue/ban", Map.of("message", "You have been banned"));
    }

  }

  @Transactional
  public void unbanUsers(UnbanRequest unbanRequest) {

    AuthenticatedUser admin = getRequiredAuthenticatedUser();

    List<UserBan> userBans = userBanRepository.findBanWithUser(unbanRequest.getIds());
    Instant now = Instant.now();

    for (UserBan userBan : userBans) {
      if (Objects.equals(admin.id(), userBan.getUserId()))
        continue;
      userBan.setRevokedAt(now);
      userBan.setRevokedBy(admin.id());
      userBan.setRevokedReason(unbanRequest.getRevokedReason());

    }

    userRepository.revokeBan(unbanRequest.getIds());
    userBanRepository.saveAll(userBans);
  }

  @Transactional
  public void deleteOwnAccount() {

    AuthenticatedUser user = getRequiredAuthenticatedUser();
    deleteUsersByIds(List.of(user.id()));

  }

  private void deleteUsersByIds(List<UUID> ids) {

    // refreshTokenRepository.deleteAllByUserIds(ids);
    // oAuthService.deleteAllByUserIds(ids);
    // reviewService.deleteAllByUserIds(ids);
    // offerService.deleteAllByUserIds(ids);
    // itemService.deleteItemsByUserIds(ids);
    // notificationService.deleteAllByUserIds(ids);
    // blogService.deleteAllByUserIds(ids);

    userBanRepository.deleteAllByUserIds(ids);

    userRepository.deleteAllById(ids);

  }

  private AuthenticatedUser getRequiredAuthenticatedUser() {
    return OwnMapper.map(currentUser.getRequiredAuthenticatedUser(), (user) -> new AuthenticatedUser(
        user.id(),
        user.email(),
        user.nickname(),
        user.password(),
        UserRole.valueOf(user.role().name()),
        user.banActive(),
        user.bannedUntil(),
        user.joinedAt()));
  }

}
