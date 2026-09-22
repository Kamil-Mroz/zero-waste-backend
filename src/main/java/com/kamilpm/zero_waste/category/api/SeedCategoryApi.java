package com.kamilpm.zero_waste.category.api;

import java.util.List;

import org.springframework.stereotype.Service;

import com.kamilpm.zero_waste.category.entity.Category;
import com.kamilpm.zero_waste.category.repository.CategoryRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SeedCategoryApi {

  private final CategoryRepository categoryRepository;

  public void checkExistsDummyCategories() {

    if (!categoryRepository.existsByName("Books")) {

      System.out.println("Start seeding categories...");

      seedCategories();
      System.out.println("Seeding categories completed.");
    }

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
