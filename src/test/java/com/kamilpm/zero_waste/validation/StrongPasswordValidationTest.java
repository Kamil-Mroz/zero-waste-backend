package com.kamilpm.zero_waste.validation;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.kamilpm.zero_waste.domain.request.RegisterRequest;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

public class StrongPasswordValidationTest {

  private Validator validator;

  @BeforeEach
  void setUp() {
    ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
    validator = factory.getValidator();
  }

  @Test
  void validStrongPasswordShouldPass() {
    var register = RegisterRequest.builder()
        .firstName("John")
        .lastName("Doe")
        .phoneNumber("+39 342 341 235")
        .email("john.doe@example.com")
        .location("Texas")
        .password("SecurePassword123!")
        .build();
    var violations = validator.validate(register);

    assertEquals(true, violations.isEmpty());
  }

  @Test
  void validStrongPasswordShouldFail() {
    var register = RegisterRequest.builder()
        .firstName("John")
        .lastName("Doe")
        .phoneNumber("+39 342 341 235")
        .email("john.doe@example.com")
        .location("Texas")
        .password("SecurePassword123")
        .build();
    var violations = validator.validate(register);

    assertEquals(1, violations.size());
    assertEquals("Password requires at least one: [a-z], [A-z], [0-9], [@$!%*?&]",
        violations.iterator().next().getMessage());
  }
}
