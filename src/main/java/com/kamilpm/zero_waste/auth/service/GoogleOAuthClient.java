package com.kamilpm.zero_waste.auth.service;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

import com.kamilpm.zero_waste.auth.dto.GoogleTokenResponse;
import com.kamilpm.zero_waste.auth.dto.GoogleUserInfo;
import com.kamilpm.zero_waste.auth.properties.OAuthProperties;

@Service
public class GoogleOAuthClient {
  private final RestClient restClient;
  private final OAuthProperties properties;

  public GoogleOAuthClient(RestClient.Builder builder, OAuthProperties properties) {
    this.properties = properties;
    this.restClient = builder.build();
  }

  public GoogleTokenResponse exchangeCode(
      String code) {

    OAuthProperties.Google google = properties.google();

    MultiValueMap<String, String> form = new LinkedMultiValueMap<>();

    form.add("client_id", google.clientId());
    form.add("client_secret", google.clientSecret());
    form.add("code", code);
    form.add("redirect_uri", google.redirectUri());
    form.add("grant_type", "authorization_code");

    return restClient
        .post()
        .uri(google.tokenUri())
        .contentType(
            MediaType.APPLICATION_FORM_URLENCODED)
        .body(form)
        .retrieve()
        .body(GoogleTokenResponse.class);
  }

  public GoogleUserInfo getUserInfo(
      String accessToken) {

    return restClient
        .get()
        .uri(properties.google().userUri())
        .header(
            HttpHeaders.AUTHORIZATION,
            "Bearer " + accessToken)
        .retrieve()
        .body(GoogleUserInfo.class);
  }
}
