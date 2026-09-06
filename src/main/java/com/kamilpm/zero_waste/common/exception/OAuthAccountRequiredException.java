package com.kamilpm.zero_waste.common.exception;

import org.springframework.http.HttpStatus;

public class OAuthAccountRequiredException extends ApiException {
  public OAuthAccountRequiredException(String message) {
    super(message, HttpStatus.CONFLICT);
  }

}
