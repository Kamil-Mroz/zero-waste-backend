package com.kamilpm.zero_waste.exception;

import org.springframework.http.HttpStatus;

public class BadCredentialsExceptionCustom extends ApiException {

  public BadCredentialsExceptionCustom(String message) {
    super(message, HttpStatus.UNAUTHORIZED);
  }

}
