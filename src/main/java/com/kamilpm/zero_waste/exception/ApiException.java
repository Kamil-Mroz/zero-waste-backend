package com.kamilpm.zero_waste.exception;

import org.springframework.http.HttpStatusCode;

import lombok.Getter;

@Getter
public class ApiException extends RuntimeException {
  private final HttpStatusCode statusCode;

  public ApiException(String message, HttpStatusCode statusCode) {
    super(message);
    this.statusCode = statusCode;
  }

}
