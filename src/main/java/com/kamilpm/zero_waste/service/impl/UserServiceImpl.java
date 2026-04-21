package com.kamilpm.zero_waste.service.impl;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.kamilpm.zero_waste.domain.entity.User;
import com.kamilpm.zero_waste.domain.entity.UserBan;
import com.kamilpm.zero_waste.domain.request.BanRequest;
import com.kamilpm.zero_waste.domain.request.CreateUserRequest;
import com.kamilpm.zero_waste.domain.request.UnbanRequest;
import com.kamilpm.zero_waste.domain.request.UpdateUserRequest;
import com.kamilpm.zero_waste.exception.ConflictException;
import com.kamilpm.zero_waste.exception.EntityNotFoundException;
import com.kamilpm.zero_waste.exception.ForbiddenException;
import com.kamilpm.zero_waste.repository.UserBanRepository;
import com.kamilpm.zero_waste.repository.UserRepository;
import com.kamilpm.zero_waste.service.AuthService;
import com.kamilpm.zero_waste.service.UserService;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

  private final UserRepository userRepository;
  private final AuthService authService;
  private final PasswordEncoder passwordEncoder;
  private final UserBanRepository userBanRepository;

  @Override
  public List<User> getUsers() {
    final User user = authService.getRequiredAuthenticatedUser();
    return userRepository.findByIdNot(user.getId());
  }

  @Override
  public User createUser(final CreateUserRequest userRequest) {

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

    return userRepository.save(user);
  }

  @Override
  public User getUser(final UUID id) {
    return userRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("User not found"));
  }

  @Override
  public User updateUser(final UUID id, final UpdateUserRequest userRequest) {

    final User admin = authService.getRequiredAuthenticatedUser();

    if (Objects.equals(admin.getId(), id)) {
      throw new ForbiddenException("You can not update your account");
    }

    if (userRepository.existsByEmailAndIdNot(userRequest.getEmail(), id)) {
      throw new ConflictException("Email already in use", "email");
    }

    final User user = getUser(id);

    user.setFirstName(userRequest.getFirstName());
    user.setLastName(userRequest.getLastName());
    user.setEmail(userRequest.getEmail());
    user.setPhoneNumber(userRequest.getPhoneNumber());
    user.setRoles(userRequest.getRoles());
    if (userRequest.getEmail() != null && !userRequest.getPassword().isBlank()) {
      user.setPassword(passwordEncoder.encode(userRequest.getPassword()));
    }
    return userRepository.save(user);
  }

  @Override
  public void deleteUser(final List<UUID> ids) {

    final User admin = authService.getRequiredAuthenticatedUser();

    if (ids.stream().anyMatch((id) -> Objects.equals(admin.getId(), id))) {
      throw new ForbiddenException("You can not delete your account");
    }

    userRepository.deleteAllById(ids);

  }

  @Override
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

    }

    userBanRepository.saveAll(bans);
    userRepository.saveAll(users);

  }

  @Override
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
