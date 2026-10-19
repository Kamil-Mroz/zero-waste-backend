package com.kamilpm.zero_waste.category.config;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Configuration;

import com.kamilpm.zero_waste.category.api.SeedCategoryApi;


@Configuration
public class CategorySeeder implements ApplicationRunner {
  private final SeedCategoryApi seedCategoryApi;



  public CategorySeeder(SeedCategoryApi seedCategoryApi) {
    this.seedCategoryApi = seedCategoryApi;
  }



  @Override
  public void run(ApplicationArguments args) throws Exception {
    seedCategoryApi.checkExistsDummyCategories();
  }

}
