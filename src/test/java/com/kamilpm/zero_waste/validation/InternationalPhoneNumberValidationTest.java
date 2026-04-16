package com.kamilpm.zero_waste.validation;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.kamilpm.zero_waste.domain.request.RegisterRequest;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

public class InternationalPhoneNumberValidationTest {
  private Validator validator;

  @BeforeEach
  void setUp() {
    ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
    validator = factory.getValidator();
  }

  @Test
  void validInternationalPhoneNumberShouldPass() {
    var register = RegisterRequest.builder()
        .firstName("John")
        .lastName("Doe")
        .phoneNumber("+39 342 341 235")
        .email("john.doe@example.com")
        .password("SecurePassword123!")
        .build();
    var violations = validator.validate(register);

    assertEquals(true, violations.isEmpty());
  }

  @Test
  void validInternationalPhoneNumberShouldFail() {
    var register = RegisterRequest.builder()
        .firstName("John")
        .lastName("Doe")
        .phoneNumber("123 123")
        .email("john.doe@example.com")
        .password("SecurePassword123!")
        .build();
    var violations = validator.validate(register);

    assertEquals(1, violations.size());
    assertEquals("Must be a valid phone number", violations.iterator().next().getMessage());
  }

}
