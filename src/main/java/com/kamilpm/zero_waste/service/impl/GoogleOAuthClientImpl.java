package com.kamilpm.zero_waste.service.impl;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

import com.kamilpm.zero_waste.config.OAuthProperties;
import com.kamilpm.zero_waste.domain.dto.GoogleTokenResponse;
import com.kamilpm.zero_waste.domain.dto.GoogleUserInfo;
import com.kamilpm.zero_waste.service.GoogleOAuthClient;

@Service
public class GoogleOAuthClientImpl implements GoogleOAuthClient {
  private final RestClient restClient;
  private final OAuthProperties properties;

  public GoogleOAuthClientImpl(RestClient.Builder builder, OAuthProperties properties) {
    this.properties = properties;
    this.restClient = builder.build();
  }

  @Override
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

  @Override
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
