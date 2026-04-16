package com.kamilpm.zero_waste.exception;

import org.springframework.http.HttpStatus;

import lombok.Getter;

@Getter
public class ConflictException extends ApiException {
  private final String fieldError;

  public ConflictException(String message, String fieldError) {
    super(message, HttpStatus.CONFLICT);
    this.fieldError = fieldError;
  }

  public ConflictException(String message) {
    super(message, HttpStatus.CONFLICT);
    this.fieldError = null;

  }

}
