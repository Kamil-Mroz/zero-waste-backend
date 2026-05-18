package com.kamilpm.zero_waste.config;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.kamilpm.zero_waste.domain.entity.Category;
import com.kamilpm.zero_waste.domain.entity.User;
import com.kamilpm.zero_waste.domain.entity.UserRole;
import com.kamilpm.zero_waste.repository.CategoryRepository;
import com.kamilpm.zero_waste.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class Seeder implements ApplicationRunner {
  private final UserRepository userRepository;
  private final CategoryRepository categoryRepository;
  private final PasswordEncoder passwordEncoder;

  @Override
  public void run(ApplicationArguments args) throws Exception {
    // System.out.println("Start seeding...");
    // seedUsers();
    // seedCategories();
    // System.out.println("Seeding completed.");

  }

  private void seedUsers() {
    List<User> users = new ArrayList<>();

    users.add(
        User.builder()
            .firstName("John")
            .lastName("Doe")
            .email("john.doe@example.com")
            .password(passwordEncoder.encode("SecurePassword123!"))
            .phoneNumber("23912123")
            .roles(Set.of(UserRole.ADMIN))
            .banActive(false)
            .bannedUntil(null)
            .build());
    users.add(
        User.builder()
            .firstName("John1")
            .lastName("Doe1")
            .email("john.doe1@example.com")
            .password(passwordEncoder.encode("SecurePassword123!"))
            .phoneNumber("23912123")
            .roles(Set.of(UserRole.USER))
            .banActive(false)
            .bannedUntil(null)
            .build());
    users.add(
        User.builder()
            .firstName("John2")
            .lastName("Doe2")
            .email("john.doe2@example.com")
            .password(passwordEncoder.encode("SecurePassword123!"))
            .phoneNumber("23912123")
            .roles(Set.of(UserRole.WRITER))
            .banActive(false)
            .bannedUntil(null)
            .build());

    for (int i = 1; i <= 50; i++) {
      users.add(
          User.builder()
              .firstName("User" + i)
              .lastName("Nick" + i)
              .email("user" + i + "@example.com")
              .password(passwordEncoder.encode("SecurePassword123!"))
              .phoneNumber("23912123")
              .roles(Set.of(UserRole.USER))
              .banActive(false)
              .bannedUntil(null)
              .build());
    }
    userRepository.saveAll(users);

  }

  private void seedCategories() {

    Category books = Category.builder()
        .name("Books")
        .build();

    Category furniture = Category.builder()
        .name("Furniture")
        .build();

    Category electronics = Category.builder()
        .name("Electronics")
        .build();

    Category clothing = Category.builder()
        .name("Clothing")
        .build();

    Category phones = Category.builder()
        .name("Phones")
        .parent(electronics)
        .build();

    Category mensWear = Category.builder()
        .name("Men's Wear")
        .parent(clothing)
        .build();

    Category home = Category.builder()
        .name("Home")
        .build();

    Category sports = Category.builder()
        .name("Sports")
        .build();

    Category kitchen = Category.builder()
        .name("Kitchen")
        .parent(home)
        .build();

    Category decor = Category.builder()
        .name("Decor")
        .parent(home)
        .build();

    Category bedding = Category.builder()
        .name("Bedding")
        .parent(home)
        .build();

    Category fitness = Category.builder()
        .name("Fitness")
        .parent(sports)
        .build();
    Category gym = Category.builder()
        .name("Gym")
        .parent(fitness)
        .build();
    Category calisthenics = Category.builder()
        .name("Calisthenics")
        .parent(fitness)
        .build();

    Category outdoor = Category.builder()
        .name("Outdoor")
        .parent(sports)
        .build();

    Category teamSports = Category.builder()
        .name("Team Sports")
        .parent(sports)
        .build();

    categoryRepository.saveAll(List.of(
        books,
        furniture,
        electronics,
        phones,
        clothing,
        mensWear,
        home,
        kitchen,
        decor,
        bedding,
        sports,
        fitness,
        gym,
        calisthenics,
        outdoor,
        teamSports));
  }

}
