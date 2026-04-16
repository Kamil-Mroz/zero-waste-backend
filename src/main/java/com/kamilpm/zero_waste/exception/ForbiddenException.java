package com.kamilpm.zero_waste.exception;

import org.springframework.http.HttpStatus;

public class ForbiddenException extends ApiException {

  public ForbiddenException(String message) {
    super(message, HttpStatus.FORBIDDEN);
  }

}
