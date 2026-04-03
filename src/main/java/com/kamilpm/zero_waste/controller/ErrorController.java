package com.kamilpm.zero_waste.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.kamilpm.zero_waste.exception.BadCredentialsExceptionCustom;
import com.kamilpm.zero_waste.exception.NotFoundException;
import com.kamilpm.zero_waste.exception.TokenException;

import lombok.extern.slf4j.Slf4j;

@ControllerAdvice
@RestControllerAdvice
@Slf4j
public class ErrorController {

  @ExceptionHandler(BadCredentialsExceptionCustom.class)
  public ProblemDetail handleTokenException(BadCredentialsExceptionCustom ex) {
    log.error("Caught bad credentials exception: Global Error Handler" + ex.getMessage());
    return ProblemDetail.forStatusAndDetail(ex.getStatusCode(), ex.getMessage());
  }

  @ExceptionHandler(TokenException.class)
  public ProblemDetail handleTokenException(TokenException ex) {
    log.error("Caught token exception: Global Error Handler" + ex.getMessage());
    return ProblemDetail.forStatusAndDetail(ex.getStatusCode(), ex.getMessage());
  }

  @ExceptionHandler(NotFoundException.class)
  public ProblemDetail handleTokenException(NotFoundException ex) {
    log.error("Caught not found exception exception: Global Error Handler" + ex.getMessage());
    return ProblemDetail.forStatusAndDetail(ex.getStatusCode(), ex.getMessage());
  }

  @ExceptionHandler(Exception.class)
  public ProblemDetail handleException(Exception ex) {
    log.error("Caught unexpected exception: Global Error Handler " + ex.getMessage());

    return ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());

  }
}
