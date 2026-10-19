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
    User user = new User();
    user.setNickname("test-123");
    user.setEmail(email);
    user.setPassword(null);
    user.setRole(UserRole.USER);
    user.setBanActive(false);
    user.setBannedUntil(null);
    userRepository.save(user);
    assertThat(userRepository.existsByEmail(email)).isTrue();

  }

  @Test
  void testExistsByEmailAndIdNot() {
    String email = "test@example.com";
    User user = new User();
    user.setNickname("test-123");
    user.setEmail(email);
    user.setPassword(null);
    user.setRole(UserRole.USER);
    user.setBanActive(false);
    user.setBannedUntil(null);
    User savedUser = userRepository.save(user);
    assertThat(userRepository.existsByEmailAndIdNot(email, savedUser.getId())).isFalse();

  }

  @Test
  void testExistsByIdAndIdNot() {
    String email = "test@example.com";
    User user = new User();
    user.setNickname("test-123");
    user.setEmail(email);
    user.setPassword(null);
    user.setRole(UserRole.USER);
    user.setBanActive(false);
    user.setBannedUntil(null);
    User savedUser = userRepository.save(user);
    assertThat(userRepository.existsByIdAndIdNot(savedUser.getId(), savedUser.getId())).isFalse();

  }

  @Test
  void testFindAllByIdNot() {

    User user1 = new User();
    user1.setNickname("test-123");
    user1.setEmail("test1@example.com");
    user1.setPassword(null);
    user1.setRole(UserRole.USER);
    user1.setBanActive(false);
    user1.setBannedUntil(null);

    User user2 = new User();
    user2.setNickname("test-123");
    user2.setEmail("test2@example.com");
    user2.setPassword(null);
    user2.setRole(UserRole.USER);
    user2.setBanActive(false);
    user2.setBannedUntil(null);

    User user3 = new User();
    user3.setNickname("test-123");
    user3.setEmail("test3@example.com");
    user3.setPassword(null);
    user3.setRole(UserRole.USER);
    user3.setBanActive(false);
    user3.setBannedUntil(null);

    User savedUser = userRepository.save(user1);
    userRepository.saveAll(List.of(user2, user3));
    Page<User> users = userRepository.findAllByIdNot(savedUser.getId(), null, null, PageRequest.of(0, 20));
    assertThat(users.getContent().size()).isEqualTo(2);
  }

  @Test
  void testRevokeBan() {

    String email = "test@example.com";
    User user = new User();
    user.setNickname("test-123");
    user.setEmail(email);
    user.setPassword(null);
    user.setRole(UserRole.USER);
    user.setBanActive(true);
    user.setBannedUntil(Instant.now().plus(10, ChronoUnit.DAYS));
    User savedUser = userRepository.save(user);
    userRepository.revokeBan(List.of(savedUser.getId()));
    Optional<User> storedUser = userRepository.findByEmail(email);
    assertThat(storedUser).isPresent();
    assertThat(storedUser.get().isBanActive()).isFalse();

  }

  @Test
  void testUpdatePassword() {
    String email = "test@example.com";
    User user = new User();
    user.setNickname("test-123");
    user.setEmail(email);
    user.setPassword("test");
    user.setRole(UserRole.USER);
    user.setBanActive(true);
    user.setBannedUntil(Instant.now().plus(10, ChronoUnit.DAYS));
    User savedUser = userRepository.save(user);

    assertThat(savedUser.getPassword()).isEqualTo("test");

    userRepository.updatePassword(savedUser.getId(), "NewPassword");
    Optional<User> storedUser = userRepository.findByEmail(email);
    assertThat(storedUser).isPresent();
    assertThat(storedUser.get().getPassword()).isEqualTo("NewPassword");

  }
}
