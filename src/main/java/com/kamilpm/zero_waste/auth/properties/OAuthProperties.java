package com.kamilpm.zero_waste.auth.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.security.oauth")
public record OAuthProperties(
    String frontendUrl,
    Github github,
    Google google) {

  public record Github(
      String authorizationUri,
      String tokenUri,
      String userUri,
      String emailsUri,
      String clientId,
      String clientSecret,
      String redirectUri) {

  }

  public record Google(
      String authorizationUri,
      String tokenUri,
      String userUri,
      String clientId,
      String clientSecret,
      String redirectUri) {
  }

}
