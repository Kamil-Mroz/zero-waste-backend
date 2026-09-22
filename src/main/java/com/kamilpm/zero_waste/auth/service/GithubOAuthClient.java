package com.kamilpm.zero_waste.auth.service;

import java.util.List;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

import com.kamilpm.zero_waste.auth.dto.GithubEmail;
import com.kamilpm.zero_waste.auth.dto.GithubTokenResponse;
import com.kamilpm.zero_waste.auth.dto.GithubUserInfo;
import com.kamilpm.zero_waste.auth.properties.OAuthProperties;

@Service
public class GithubOAuthClient {

  private final RestClient restClient;
  private final OAuthProperties properties;

  public GithubOAuthClient(RestClient.Builder builder, OAuthProperties properties) {
    this.properties = properties;
    this.restClient = builder.build();
  }

  public GithubTokenResponse exchangeCode(
      String code) {

    OAuthProperties.Github github = properties.github();

    MultiValueMap<String, String> form = new LinkedMultiValueMap<>();

    form.add(
        "client_id",
        github.clientId());

    form.add(
        "client_secret",
        github.clientSecret());

    form.add(
        "code",
        code);

    form.add(
        "redirect_uri",
        github.redirectUri());

    return restClient
        .post()
        .uri(github.tokenUri())
        .contentType(
            MediaType.APPLICATION_FORM_URLENCODED)
        .header(
            "Accept",
            MediaType.APPLICATION_JSON_VALUE)
        .body(form)
        .retrieve()
        .body(GithubTokenResponse.class);
  }

  public GithubUserInfo getUser(
      String accessToken) {

    return restClient
        .get()
        .uri(properties.github().userUri())
        .header(
            HttpHeaders.AUTHORIZATION,
            "Bearer " + accessToken)
        .header(
            HttpHeaders.ACCEPT,
            MediaType.APPLICATION_JSON_VALUE)
        .retrieve()
        .body(GithubUserInfo.class);
  }

  public List<GithubEmail> getEmails(
      String accessToken) {

    return restClient
        .get()
        .uri(properties.github().emailsUri())
        .header(
            HttpHeaders.AUTHORIZATION,
            "Bearer " + accessToken)
        .header(
            HttpHeaders.ACCEPT,
            MediaType.APPLICATION_JSON_VALUE)
        .retrieve()
        .body(
            new ParameterizedTypeReference<>() {
            });
  }
}
