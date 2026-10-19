package com.kamilpm.zero_waste.user.dto;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.kamilpm.zero_waste.common.dto.UserRole;
import com.kamilpm.zero_waste.user.entity.User;
import com.kamilpm.zero_waste.user.repository.UserRepository;

@Service
public class SeedUserApi {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;

  public SeedUserApi(UserRepository userRepository,
      PasswordEncoder passwordEncoder) {
    this.userRepository = userRepository;
    this.passwordEncoder = passwordEncoder;

  }

  @Value("${app.security.passwords.admin}")
  private String adminPassword;
  @Value("${app.security.passwords.users}")
  private String userPassword;

  public void checkExistsDummyUsers() {

    if (!userRepository.existsByEmail("john.doe@example.com")) {
      System.out.println("Start seeding users...");

      seedUsers();
      System.out.println("Seeding users completed.");
    }

  }

  private void seedUsers() {
    List<User> users = new ArrayList<>();

    var user1 = new User();
    user1.setNickname("JohnDoe");
    user1.setEmail("john.doe@example.com");
    user1.setPassword(passwordEncoder.encode(adminPassword));
    user1.setRole(UserRole.ADMIN);
    user1.setBanActive(false);
    user1.setBannedUntil(null);

    users.add(user1);

    var user2 = new User();
    user2.setNickname("JohnDoe1");
    user2.setEmail("john.doe1@example.com");
    user2.setPassword(passwordEncoder.encode(userPassword));
    user2.setRole(UserRole.USER);
    user2.setBanActive(false);
    user2.setBannedUntil(null);
    users.add(user2);

    var user3 = new User();
    user3.setNickname("JohnDoe2");
    user3.setEmail("john.doe2@example.com");
    user3.setPassword(passwordEncoder.encode(userPassword));
    user3.setRole(UserRole.WRITER);
    user3.setBanActive(false);
    user3.setBannedUntil(null);

    users.add(user3);

    for (int i = 1; i <= 20; i++) {
      var user = new User();

      user.setNickname("User" + i);
      user.setEmail("user" + i + "@example.com");
      user.setPassword(passwordEncoder.encode(userPassword));
      user.setRole(UserRole.USER);
      user.setBanActive(false);
      user.setBannedUntil(null);
      users.add(
          user);
    }
    userRepository.saveAll(users);

  }

}
