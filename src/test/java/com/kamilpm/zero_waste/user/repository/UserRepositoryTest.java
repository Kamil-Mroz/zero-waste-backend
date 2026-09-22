package com.kamilpm.zero_waste.user.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import com.kamilpm.zero_waste.common.dto.UserRole;
import com.kamilpm.zero_waste.config.PostgresTestConfiguration;
import com.kamilpm.zero_waste.user.entity.User;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

@DataJpaTest
@Import(PostgresTestConfiguration.class)
public class UserRepositoryTest {

  @Autowired
  private UserRepository userRepository;

  @Test
  void testExistsByEmail() {
    String email = "test@example.com";
    User user = User.builder()
        .nickname("test-123")
        .email(email)
        .password(null)
        .role(UserRole.USER)
        .banActive(false)
        .bannedUntil(null)
        .build();
    userRepository.save(user);
    assertThat(userRepository.existsByEmail(email)).isTrue();

  }

  @Test
  void testExistsByEmailAndIdNot() {
    String email = "test@example.com";
    User user = User.builder()
        .nickname("test-123")
        .email(email)
        .password(null)
        .role(UserRole.USER)
        .banActive(false)
        .bannedUntil(null)
        .build();
    User savedUser = userRepository.save(user);
    assertThat(userRepository.existsByEmailAndIdNot(email, savedUser.getId())).isFalse();

  }

  @Test
  void testExistsByIdAndIdNot() {
    String email = "test@example.com";
    User user = User.builder()
        .nickname("test-123")
        .email(email)
        .password(null)
        .role(UserRole.USER)
        .banActive(false)
        .bannedUntil(null)
        .build();
    User savedUser = userRepository.save(user);
    assertThat(userRepository.existsByIdAndIdNot(savedUser.getId(), savedUser.getId())).isFalse();

  }

  @Test
  void testFindAllByIdNot() {

    User user1 = User.builder()
        .nickname("test-123")
        .email("test1@example.com")
        .password(null)
        .role(UserRole.USER)
        .banActive(false)
        .bannedUntil(null)
        .build();
    User user2 = User.builder()
        .nickname("test-123")
        .email("test2@example.com")
        .password(null)
        .role(UserRole.USER)
        .banActive(false)
        .bannedUntil(null)
        .build();
    User user3 = User.builder()
        .nickname("test-123")
        .email("test3@example.com")
        .password(null)
        .role(UserRole.USER)
        .banActive(false)
        .bannedUntil(null)
        .build();
    User savedUser = userRepository.save(user1);
    userRepository.saveAll(List.of(user2, user3));
    Page<User> users = userRepository.findAllByIdNot(savedUser.getId(), null, null, PageRequest.of(0, 20));
    assertThat(users.getContent().size()).isEqualTo(2);
  }

  @Test
  void testRevokeBan() {

    String email = "test@example.com";
    User user = User.builder()
        .nickname("test-123")
        .email(email)
        .password(null)
        .role(UserRole.USER)
        .banActive(true)
        .bannedUntil(Instant.now().plus(10, ChronoUnit.DAYS))
        .build();
    User savedUser = userRepository.save(user);
    userRepository.revokeBan(List.of(savedUser.getId()));
    Optional<User> storedUser = userRepository.findByEmail(email);
    assertThat(storedUser).isPresent();
    assertThat(storedUser.get().isBanActive()).isFalse();

  }

  @Test
  void testUpdatePassword() {
    String email = "test@example.com";
    User user = User.builder()
        .nickname("test-123")
        .email(email)
        .password("test")
        .role(UserRole.USER)
        .banActive(true)
        .bannedUntil(Instant.now().plus(10, ChronoUnit.DAYS))
        .build();
    User savedUser = userRepository.save(user);

    assertThat(savedUser.getPassword()).isEqualTo("test");

    userRepository.updatePassword(savedUser.getId(), "NewPassword");
    Optional<User> storedUser = userRepository.findByEmail(email);
    assertThat(storedUser).isPresent();
    assertThat(storedUser.get().getPassword()).isEqualTo("NewPassword");

  }
}
