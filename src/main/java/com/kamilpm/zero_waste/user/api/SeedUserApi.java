package com.kamilpm.zero_waste.user.api;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.kamilpm.zero_waste.user.entity.User;
import com.kamilpm.zero_waste.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SeedUserApi {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;

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

    users.add(
        User.builder()
            .nickname("JohnDoe")
            .email("john.doe@example.com")
            .password(passwordEncoder.encode(adminPassword))
            .role(UserRole.ADMIN)
            .banActive(false)
            .bannedUntil(null)
            .build());
    users.add(
        User.builder()
            .nickname("JohnDoe1")
            .email("john.doe1@example.com")
            .password(passwordEncoder.encode(userPassword))
            .role(UserRole.USER)
            .banActive(false)
            .bannedUntil(null)
            .build());
    users.add(
        User.builder()
            .nickname("JohnDoe2")
            .email("john.doe2@example.com")
            .password(passwordEncoder.encode(userPassword))
            .role(UserRole.WRITER)
            .banActive(false)
            .bannedUntil(null)
            .build());

    for (int i = 1; i <= 20; i++) {
      users.add(
          User.builder()
              .nickname("User" + i)
              .email("user" + i + "@example.com")
              .password(passwordEncoder.encode(userPassword))
              .role(UserRole.USER)
              .banActive(false)
              .bannedUntil(null)
              .build());
    }
    userRepository.saveAll(users);

  }

}
