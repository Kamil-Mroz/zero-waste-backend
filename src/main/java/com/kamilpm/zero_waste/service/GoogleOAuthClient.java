package com.kamilpm.zero_waste.service;

import com.kamilpm.zero_waste.domain.dto.GoogleTokenResponse;
import com.kamilpm.zero_waste.domain.dto.GoogleUserInfo;

public interface GoogleOAuthClient {

  GoogleTokenResponse exchangeCode(
      String code);

  GoogleUserInfo getUserInfo(
      String accessToken);

}
