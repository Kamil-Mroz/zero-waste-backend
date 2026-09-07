package com.kamilpm.zero_waste.user.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Configuration;

import com.kamilpm.zero_waste.user.api.SeedUserApi;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class UserSeeder implements ApplicationRunner {

  private final SeedUserApi seedUserApi;

  @Value("${app.prod}")
  private boolean isProd;

  @Override
  public void run(ApplicationArguments args) throws Exception {
    if (!isProd) {
      seedUserApi.checkExistsDummyUsers();
    }

  }
}
