package com.kamilpm.zero_waste.category.api;

import java.util.List;

import org.springframework.stereotype.Service;

import com.kamilpm.zero_waste.category.entity.Category;
import com.kamilpm.zero_waste.category.repository.CategoryRepository;

@Service
public class SeedCategoryApi {

  private final CategoryRepository categoryRepository;

  public SeedCategoryApi(CategoryRepository categoryRepository) {
    this.categoryRepository = categoryRepository;
  }

  public void checkExistsDummyCategories() {

    if (!categoryRepository.existsByName("Books")) {

      System.out.println("Start seeding categories...");

      seedCategories();
      System.out.println("Seeding categories completed.");
    }

  }

  private void seedCategories() {

    Category books = new Category();
    books.setName("Books");

    Category furniture = new Category();
    furniture.setName("Furniture");

    Category electronics = new Category();
    electronics.setName("Electronics");

    Category clothing = new Category();
    clothing.setName("Clothing");

    Category phones = new Category();
    phones.setName("Phones");
    phones.setParent(electronics);

    Category mensWear = new Category();
    mensWear.setName("Men's Wear");
    mensWear.setParent(clothing);

    Category home = new Category();
    home.setName("Home");

    Category sports = new Category();
    sports.setName("Sports");

    Category kitchen = new Category();
    kitchen.setName("Kitchen");
    kitchen.setParent(home);

    Category decor = new Category();
    decor.setName("Decor");
    decor.setParent(home);

    Category bedding = new Category();
    bedding.setName("Bedding");
    bedding.setParent(home);

    Category fitness = new Category();
    fitness.setName("Fitness");
    fitness.setParent(sports);
    Category gym = new Category();
    gym.setName("Gym");
    gym.setParent(fitness);

    Category calisthenics = new Category();
    calisthenics.setName("Calisthenics");
    calisthenics.setParent(fitness);

    Category outdoor = new Category();
    outdoor.setName("Outdoor");
    outdoor.setParent(sports);

    Category teamSports = new Category();
    teamSports.setName("Team Sports");
    teamSports.setParent(sports);

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
