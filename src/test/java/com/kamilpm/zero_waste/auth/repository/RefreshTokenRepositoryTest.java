package com.kamilpm.zero_waste.auth.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;

import com.kamilpm.zero_waste.auth.entity.RefreshToken;
import com.kamilpm.zero_waste.common.dto.UserRole;
import com.kamilpm.zero_waste.config.PostgresTestConfiguration;
import com.kamilpm.zero_waste.user.entity.User;
import com.kamilpm.zero_waste.user.repository.UserRepository;

@DataJpaTest
@Import(PostgresTestConfiguration.class)
public class RefreshTokenRepositoryTest {

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private RefreshTokenRepository refreshTokenRepository;

  @Test
  void testDeleteAllByUserIds() {

    String email = "test@example.com";
    User user = User.builder()
        .nickname("test-123")
        .email(email)
        .password(null)
        .role(UserRole.USER)
        .banActive(false)
        .bannedUntil(null)
        .build();

    User saveUser = userRepository.save(user);

    RefreshToken token = RefreshToken.builder()
        .userId(saveUser.getId())
        .token(UUID.randomUUID().toString())
        .expiryDate(Instant.now().plus(7, ChronoUnit.DAYS))
        .revoked(false)
        .build();

    RefreshToken savedToken = refreshTokenRepository.save(token);

    refreshTokenRepository.deleteAllByUserIds(List.of(saveUser.getId()));

    Optional<RefreshToken> updatedToken = refreshTokenRepository.findByToken(savedToken.getToken());
    assertThat(updatedToken.isEmpty()).isTrue();

  }

  @Test
  void testRevokeAllByUserIds() {

    String email = "test@example.com";
    User user = User.builder()
        .nickname("test-123")
        .email(email)
        .password(null)
        .role(UserRole.USER)
        .banActive(false)
        .bannedUntil(null)
        .build();

    User saveUser = userRepository.save(user);

    RefreshToken token = RefreshToken.builder()
        .userId(saveUser.getId())
        .token(UUID.randomUUID().toString())
        .expiryDate(Instant.now().plus(7, ChronoUnit.DAYS))
        .revoked(false)
        .build();

    RefreshToken savedToken = refreshTokenRepository.save(token);

    refreshTokenRepository.revokeAllByUserIds(List.of(saveUser.getId()));

    Optional<RefreshToken> updatedToken = refreshTokenRepository.findByToken(savedToken.getToken());
    assertThat(updatedToken.isPresent()).isTrue();
    assertThat(updatedToken.get().isRevoked()).isTrue();
  }
}
