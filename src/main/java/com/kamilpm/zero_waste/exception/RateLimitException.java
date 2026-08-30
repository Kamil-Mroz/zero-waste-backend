package com.kamilpm.zero_waste.exception;

import java.time.Duration;

import org.springframework.http.HttpStatus;

import lombok.Getter;

@Getter
public class RateLimitException extends ApiException {
  private final Duration retryAfter;

  public RateLimitException(Duration retryAfter) {
    super("You have exceeded the rate limit.", HttpStatus.TOO_MANY_REQUESTS);
    this.retryAfter = retryAfter;
  }

}
