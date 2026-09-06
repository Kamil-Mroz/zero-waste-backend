package com.kamilpm.zero_waste.common.config;

import com.kamilpm.zero_waste.category.api.SeedCategoryApi;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Configuration;

import com.kamilpm.zero_waste.user.api.SeedUserApi;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class Seeder implements ApplicationRunner {
  private final SeedCategoryApi seedCategoryApi;

  private final SeedUserApi seedUserApi;

  @Value("${app.prod}")
  private boolean isProd;

  @Override
  public void run(ApplicationArguments args) throws Exception {
    if (!isProd) {
      seedUserApi.checkExistsDummyUsers();
    }
    seedCategoryApi.checkExistsDummyCategories();

  }

}
