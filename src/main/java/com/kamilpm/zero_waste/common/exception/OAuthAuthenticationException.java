package com.kamilpm.zero_waste.common.exception;

import org.springframework.http.HttpStatus;

public class OAuthAuthenticationException extends ApiException {
  public OAuthAuthenticationException(String message) {
    super(message, HttpStatus.UNAUTHORIZED);
  }
}
