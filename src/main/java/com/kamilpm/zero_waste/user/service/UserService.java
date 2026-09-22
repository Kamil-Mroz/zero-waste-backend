package com.kamilpm.zero_waste.user.service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kamilpm.zero_waste.common.dto.CurrentUser;
import com.kamilpm.zero_waste.common.dto.UserRole;
import com.kamilpm.zero_waste.common.events.BanEvent;
import com.kamilpm.zero_waste.common.events.RevokeRefreshTokenEvent;
import com.kamilpm.zero_waste.common.events.SendBansNotificationEvent;
import com.kamilpm.zero_waste.common.events.UnbanEvent;
import com.kamilpm.zero_waste.common.events.UserRoleChangeEvent;
import com.kamilpm.zero_waste.common.exception.ConflictException;
import com.kamilpm.zero_waste.common.exception.EntityNotFoundException;
import com.kamilpm.zero_waste.common.exception.ForbiddenException;
import com.kamilpm.zero_waste.common.interfaces.CurrentUserProvider;
import com.kamilpm.zero_waste.common.interfaces.ItemProvider;
import com.kamilpm.zero_waste.common.utils.SqlUtils;
import com.kamilpm.zero_waste.user.dto.BanRequest;
import com.kamilpm.zero_waste.user.dto.CreateUserRequest;
import com.kamilpm.zero_waste.user.dto.UnbanRequest;
import com.kamilpm.zero_waste.user.dto.UpdateUserRequest;
import com.kamilpm.zero_waste.user.dto.UserDto;
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
  private final CurrentUserProvider currentUser;
  private final PasswordEncoder passwordEncoder;
  private final UserBanRepository userBanRepository;
  private final UserMapper userMapper;
  private final ApplicationEventPublisher events;
  private final ItemProvider itemApi;

  @Transactional(readOnly = true)
  public Page<UserDto> getUsersWithoutCurrentUser(String text, List<UserRole> roles, Pageable pageable) {

    if (roles != null && roles.isEmpty())
      roles = null;
    text = SqlUtils.prepareLikePattern(text);
    CurrentUser user = currentUser.getRequiredAuthenticatedUser();
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
    // if (user.isBanActive()) {
    // throw new EntityNotFoundException("User not found");
    // }

    return userMapper.toDto(user);
  }

  private User findUser(UUID id) {
    return userRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("User not found"));
  }

  @Transactional
  public UserDto updateUser(final UUID id, final UpdateUserRequest userRequest) {

    CurrentUser admin = currentUser.getRequiredAuthenticatedUser();

    if (Objects.equals(admin.id(), id)) {
      throw new ForbiddenException("You can not update your account");
    }

    if (userRepository.existsByEmailAndIdNot(userRequest.getEmail(), id)) {
      throw new ConflictException("Email already in use", "email");
    }

    final User user = findUser(id);

    UserRole oldRole = user.getRole();

    user.setNickname(userRequest.getNickname());
    user.setEmail(userRequest.getEmail());
    user.setRole(userRequest.getRole());
    if (userRequest.getEmail() != null && !userRequest.getPassword().isBlank()) {
      user.setPassword(passwordEncoder.encode(userRequest.getPassword()));
    }
    User updatedUser = userRepository.save(user);

    if (!Objects.equals(oldRole, updatedUser.getRole())) {
      events.publishEvent(new UserRoleChangeEvent(updatedUser.getId(), oldRole, updatedUser.getRole()));

    }

    return userMapper.toDto(updatedUser);
  }

  @Transactional
  public void deleteUser(final List<UUID> ids) {

    CurrentUser admin = currentUser.getRequiredAuthenticatedUser();

    // if (ids.stream().anyMatch((id) -> Objects.equals(admin.id(), id))) {
    // throw new ForbiddenException("You can not delete your account");
    // }

    deleteUsersByIds(ids.stream().filter((id) -> !Objects.equals(id, admin.id())).toList());

  }

  @Transactional
  public void banUsers(final BanRequest banRequest) {
    CurrentUser admin = currentUser.getRequiredAuthenticatedUser();

    List<User> users = userRepository.findAllById(banRequest.getIds());

    List<UserBan> bans = new ArrayList<>();
    List<UUID> bannedUserIds = new ArrayList<>();
    List<String> bannedUsersEmail = new ArrayList<>();
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
      bannedUsersEmail.add(user.getEmail());

      bans.add(
          UserBan.builder()
              .userId(user.getId())
              .reason(banRequest.getReason())
              .createdAt(now)
              .bannedBy(admin.id())
              .expiresAt(banRequest.getExpiresAt())
              .build());
    }

    userBanRepository.saveAll(bans);
    userRepository.saveAll(users);

    events.publishEvent(new RevokeRefreshTokenEvent(bannedUserIds));
    events.publishEvent(new BanEvent(bannedUserIds));
    events.publishEvent(new SendBansNotificationEvent(bannedUsersEmail));

  }

  @Transactional
  public void unbanUsers(UnbanRequest unbanRequest) {

    CurrentUser admin = currentUser.getRequiredAuthenticatedUser();

    List<UserBan> userBans = userBanRepository.findBanWithUser(unbanRequest.getIds());
    Instant now = Instant.now();
    List<UUID> unBannedUserIds = new ArrayList<>();

    for (UserBan userBan : userBans) {
      if (Objects.equals(admin.id(), userBan.getUserId()))
        continue;
      userBan.setRevokedAt(now);
      unBannedUserIds.add(userBan.getUserId());
      userBan.setRevokedBy(admin.id());
      userBan.setRevokedReason(unbanRequest.getRevokedReason());
    }

    userRepository.revokeBan(unbanRequest.getIds());
    userBanRepository.saveAll(userBans);

    events.publishEvent(new UnbanEvent(unBannedUserIds));

  }

  @Transactional
  public void deleteOwnAccount() {

    CurrentUser user = currentUser.getRequiredAuthenticatedUser();
    deleteUsersByIds(List.of(user.id()));

  }

  private void deleteUsersByIds(List<UUID> ids) {

    itemApi.deleteItemsByOwnerIds(ids);
    userRepository.deleteAllById(ids);

  }

}
