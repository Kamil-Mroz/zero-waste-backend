package com.kamilpm.zero_waste.service.impl;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kamilpm.zero_waste.domain.dto.UserDto;
import com.kamilpm.zero_waste.domain.entity.User;
import com.kamilpm.zero_waste.domain.entity.UserBan;
import com.kamilpm.zero_waste.domain.entity.UserRole;
import com.kamilpm.zero_waste.domain.mapper.UserMapper;
import com.kamilpm.zero_waste.domain.request.BanRequest;
import com.kamilpm.zero_waste.domain.request.CreateUserRequest;
import com.kamilpm.zero_waste.domain.request.UnbanRequest;
import com.kamilpm.zero_waste.domain.request.UpdateUserRequest;
import com.kamilpm.zero_waste.exception.ConflictException;
import com.kamilpm.zero_waste.exception.EntityNotFoundException;
import com.kamilpm.zero_waste.exception.ForbiddenException;
import com.kamilpm.zero_waste.repository.RefreshTokenRepository;
import com.kamilpm.zero_waste.repository.UserBanRepository;
import com.kamilpm.zero_waste.repository.UserRepository;
import com.kamilpm.zero_waste.service.AuthService;
import com.kamilpm.zero_waste.service.ItemService;
import com.kamilpm.zero_waste.service.NotificationService;
import com.kamilpm.zero_waste.service.OfferService;
import com.kamilpm.zero_waste.service.ReviewService;
import com.kamilpm.zero_waste.service.UserService;
import com.kamilpm.zero_waste.utils.SqlUtils;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

  private final UserRepository userRepository;
  private final AuthService authService;
  private final PasswordEncoder passwordEncoder;
  private final ItemService itemService;
  private final UserBanRepository userBanRepository;
  private final ReviewService reviewService;
  private final RefreshTokenRepository refreshTokenRepository;
  private final OfferService offerService;
  private final UserMapper userMapper;
  private final NotificationService notificationService;

  @Override
  @Transactional(readOnly = true)
  public Page<UserDto> getUsersWithoutCurrentUser(String text, List<UserRole> roles, Pageable pageable) {

    if (roles != null && roles.isEmpty())
      roles = null;
    text = SqlUtils.prepareLikePattern(text);
    User user = authService.getRequiredAuthenticatedUser();
    return userRepository.findAllByIdNot(user.getId(), text, roles, pageable).map(userMapper::toDto);
  }

  @Override
  @Transactional
  public UserDto createUser(final CreateUserRequest userRequest) {

    if (userRepository.existsByEmail(userRequest.getEmail())) {
      throw new ConflictException("Email already in use", "email");
    }

    final User user = User.builder()
        .firstName(userRequest.getFirstName())
        .lastName(userRequest.getLastName())
        .email(userRequest.getEmail())
        .phoneNumber(userRequest.getPhoneNumber())
        .password(passwordEncoder.encode(userRequest.getPassword()))
        .roles(userRequest.getRoles())
        .banActive(false)
        .bannedUntil(null)
        .build();

    User savedUser = userRepository.save(user);
    return userMapper.toDto(savedUser);
  }

  @Override
  @Transactional(readOnly = true)
  public UserDto getUser(final UUID id) {
    User user = findUser(id);
    return userMapper.toDto(user);
  }

  private User findUser(UUID id) {
    return userRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("User not found"));
  }

  @Override
  @Transactional
  public UserDto updateUser(final UUID id, final UpdateUserRequest userRequest) {

    User admin = authService.getRequiredAuthenticatedUser();

    if (Objects.equals(admin.getId(), id)) {
      throw new ForbiddenException("You can not update your account");
    }

    if (userRepository.existsByEmailAndIdNot(userRequest.getEmail(), id)) {
      throw new ConflictException("Email already in use", "email");
    }

    final User user = findUser(id);

    user.setFirstName(userRequest.getFirstName());
    user.setLastName(userRequest.getLastName());
    user.setEmail(userRequest.getEmail());
    user.setPhoneNumber(userRequest.getPhoneNumber());
    user.setRoles(userRequest.getRoles());
    if (userRequest.getEmail() != null && !userRequest.getPassword().isBlank()) {
      user.setPassword(passwordEncoder.encode(userRequest.getPassword()));
    }
    User updatedUser = userRepository.save(user);
    return userMapper.toDto(updatedUser);
  }

  @Override
  @Transactional
  public void deleteUser(final List<UUID> ids) {

    User admin = authService.getRequiredAuthenticatedUser();

    if (ids.stream().anyMatch((id) -> Objects.equals(admin.getId(), id))) {
      throw new ForbiddenException("You can not delete your account");
    }

    refreshTokenRepository.deleteAllByUserIds(ids);

    userBanRepository.deleteAllByUserIds(ids);

    reviewService.deleteAllByUserIds(ids);
    offerService.deleteAllByUserIds(ids);

    itemService.deleteItemsByUserIds(ids);
    notificationService.deleteAllByUserIds(ids);

    userRepository.deleteAllById(ids);

  }

  @Override
  @Transactional
  public void banUsers(final BanRequest banRequest) {
    final User admin = authService.getRequiredAuthenticatedUser();

    final List<User> users = userRepository.findAllById(banRequest.getIds());

    final List<UserBan> bans = new ArrayList<>();
    Instant now = Instant.now();

    for (final User user : users) {
      if (Objects.equals(admin.getId(), user.getId())) {
        continue;
      }

      if (user.isBanActive()) {
        continue;
      }
      user.setBanActive(true);
      user.setBannedUntil(banRequest.getExpiresAt());

      bans.add(
          UserBan.builder()
              .user(user)
              .reason(banRequest.getReason())
              .createdAt(now)
              .bannedBy(admin)
              .expiresAt(banRequest.getExpiresAt())
              .build());

      refreshTokenRepository.revokeAllByUserId(user.getId());

    }

    userBanRepository.saveAll(bans);
    userRepository.saveAll(users);

  }

  @Override
  @Transactional
  public void unbanUsers(UnbanRequest unbanRequest) {

    final User admin = authService.getRequiredAuthenticatedUser();

    List<UserBan> userBans = userBanRepository.findBanWithUser(unbanRequest.getIds());
    Instant now = Instant.now();
    List<User> users = new ArrayList<>();

    for (UserBan userBan : userBans) {
      User user = userBan.getUser();
      if (Objects.equals(admin.getId(), user.getId()))
        continue;
      userBan.setRevokedAt(now);
      userBan.setRevokedBy(admin);
      userBan.setRevokedReason(unbanRequest.getRevokedReason());

      user.setBanActive(false);
      user.setBannedUntil(null);

      users.add(user);

    }

    userRepository.saveAll(users);
    userBanRepository.saveAll(userBans);
  }
}
