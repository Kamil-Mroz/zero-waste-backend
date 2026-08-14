package com.kamilpm.zero_waste.service;

import java.util.List;

import com.kamilpm.zero_waste.domain.dto.GithubEmail;
import com.kamilpm.zero_waste.domain.dto.GithubTokenResponse;
import com.kamilpm.zero_waste.domain.dto.GithubUserInfo;

public interface GithubOAuthClient {

  GithubTokenResponse exchangeCode(
      String code);

  GithubUserInfo getUser(
      String accessToken);

  List<GithubEmail> getEmails(
      String accessToken);

}
