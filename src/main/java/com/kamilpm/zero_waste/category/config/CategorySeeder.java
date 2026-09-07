package com.kamilpm.zero_waste.category.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Configuration;

import com.kamilpm.zero_waste.category.api.SeedCategoryApi;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class CategorySeeder implements ApplicationRunner {
  private final SeedCategoryApi seedCategoryApi;

  @Override
  public void run(ApplicationArguments args) throws Exception {
    seedCategoryApi.checkExistsDummyCategories();
  }

}
