package com.kamilpm.zero_waste.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.security.authentication.LockedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import com.kamilpm.zero_waste.domain.entity.ErrorContent;
import com.kamilpm.zero_waste.exception.ApiException;
import com.kamilpm.zero_waste.exception.ConflictException;

import lombok.extern.slf4j.Slf4j;

@ControllerAdvice
@RestControllerAdvice
@Slf4j
public class ErrorController {

  @ExceptionHandler(ApiException.class)
  public ProblemDetail handleApiException(
      ApiException ex) {
    log.error(
        "Caught api exception: {}",
        ex.getMessage());

    return ProblemDetail
        .forStatusAndDetail(
            ex.getStatusCode(),
            ex.getMessage());
  }

  @ExceptionHandler(LockedException.class)
  public ProblemDetail handleLockedException(LockedException ex) {

    log.error("Caught locked exception: Global Error Handler " + ex.getMessage());

    return ProblemDetail.forStatusAndDetail(HttpStatus.FORBIDDEN, ex.getMessage());
  }

  @ExceptionHandler(ConflictException.class)
  public ProblemDetail handleConflictException(ConflictException ex) {
    log.error("Caught entity already exists exception: Global Error Handler ");
    ProblemDetail problem = ProblemDetail.forStatusAndDetail(ex.getStatusCode(), ex.getMessage());

    if (ex.getFieldError() != null) {

      Map<String, ErrorContent> fieldErrors = new HashMap<>();

      ErrorContent content = ErrorContent.builder().message(ex.getMessage()).build();
      fieldErrors.put(ex.getFieldError(), content);
      problem.setProperty("errors", fieldErrors);

    }

    return problem;
  }

  @ExceptionHandler(MethodArgumentTypeMismatchException.class)
  public ProblemDetail handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException ex) {

    log.error("Caught method argument type mismatch exception: Global Error Handler ");
    String message = "Invalid value for parameter: " + ex.getName();
    return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, message);
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ProblemDetail handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {
    log.error("Caught method argument not valid exception: Global Error Handler ");

    Map<String, ErrorContent> fieldErrors = new HashMap<>();
    for (FieldError error : ex.getBindingResult().getFieldErrors()) {
      ErrorContent content = ErrorContent.builder().message(error.getDefaultMessage()).build();
      fieldErrors.put(error.getField(), content);
    }

    ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Invalid input value");
    problem.setProperty("errors", fieldErrors);
    return problem;
  }

  @ExceptionHandler(Exception.class)
  public ProblemDetail handleException(Exception ex) {
    log.error("Caught unexpected exception: Global Error Handler " + ex.getClass().getName());

    return ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());

  }
}
