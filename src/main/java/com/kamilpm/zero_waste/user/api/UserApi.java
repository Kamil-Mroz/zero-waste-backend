package com.kamilpm.zero_waste.user.api;

import java.time.Instant;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.kamilpm.zero_waste.auth.api.AuthenticatedUser;
import com.kamilpm.zero_waste.common.exception.EntityNotFoundException;
import com.kamilpm.zero_waste.user.entity.User;
import com.kamilpm.zero_waste.user.repository.UserBanRepository;
import com.kamilpm.zero_waste.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserApi {
  private final UserRepository userRepository;
  private final UserBanRepository userBanRepository;

  @Value("${app.security.demo.email}")
  private String demoEmail;

  public UserAuthenticationData findAuthenticationData(String email) {
    User user = userRepository.findByEmail(email).orElseThrow(() -> new EntityNotFoundException("User not found"));

    clearExpiredBan(user);

    return new UserAuthenticationData(user.getId(), user.getEmail(), user.getNickname(), user.getPassword(),
        user.getRole(),
        user.isBanActive(), user.getBannedUntil(), user.getJoinedAt());

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
  }

  public Set<UUID> findExcludedUserIdsForPublicContent() {
    return userRepository.findIdsByBanActiveTrueOrRole(UserRole.DEMO);
  }

  public void savePassword(UUID userId, String passwordHash) {
    userRepository.updatePassword(userId, passwordHash);
  }

  public AuthenticatedUser getDemoUser() {
    User user = userRepository.findByEmail(demoEmail)
        .orElseThrow(() -> new EntityNotFoundException("Demo user not found"));
    return new AuthenticatedUser(user.getId(), user.getEmail(), user.getNickname(), user.getPassword(), user.getRole(),
        user.isBanActive(), user.getBannedUntil(), user.getJoinedAt());

  }

  public AuthenticatedUser findById(UUID id) {
    User user = userRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("User not found"));
    return toAuthenticatedUser(user);
  }

  public Optional<AuthenticatedUser> findAuthenticatedUserByEmail(String email) {
    return userRepository.findByEmail(email).map(this::toAuthenticatedUser);
  }

  private AuthenticatedUser toAuthenticatedUser(User user) {
    return new AuthenticatedUser(user.getId(), user.getEmail(), user.getNickname(), user.getPassword(),
        user.getRole(),
        user.isBanActive(), user.getBannedUntil(), user.getJoinedAt());
  }

  public AuthenticatedUser createOAuthUser(String email, String nickname) {
    User user = User.builder()
        .nickname(nickname)
        .email(email.toLowerCase())
        .password(null)
        .role(UserRole.USER)
        .banActive(false)
        .bannedUntil(null)
        .build();
    return toAuthenticatedUser(userRepository.save(user));
  }

}
