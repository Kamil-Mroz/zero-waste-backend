package com.kamilpm.zero_waste.common.validation;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.kamilpm.zero_waste.auth.dto.CreatePasswordRequest;

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
    var newPassword = new CreatePasswordRequest("SecurePassword123!", "SecurePassword123!");
    var violations = validator.validate(newPassword);

    assertEquals(true, violations.isEmpty());
  }

  @Test
  void validStrongPasswordShouldFailNoSpecialCharacter() {
    var newPassword = new CreatePasswordRequest("SecurePassword123", " SecurePassword123");
    var violations = validator.validate(newPassword);

    assertEquals(2, violations.size());
    assertEquals("Password requires at least one: [a-z], [A-z], [0-9], [@$!%*?&]",
        violations.iterator().next().getMessage());
  }

  @Test
  void validStrongPasswordShouldFailNoNumber() {
    var newPassword = new CreatePasswordRequest("SecurePassword!", " SecurePassword!");
    var violations = validator.validate(newPassword);

    assertEquals(2, violations.size());
    assertEquals("Password requires at least one: [a-z], [A-z], [0-9], [@$!%*?&]",
        violations.iterator().next().getMessage());
  }

  @Test
  void validStrongPasswordShouldFailNoUppercase() {
    var newPassword = new CreatePasswordRequest("securepassword123!", " securepassword123!");
    var violations = validator.validate(newPassword);

    assertEquals(2, violations.size());
    assertEquals("Password requires at least one: [a-z], [A-z], [0-9], [@$!%*?&]",
        violations.iterator().next().getMessage());
  }

  @Test
  void validStrongPasswordShouldFailNoLowercase() {
    var newPassword = new CreatePasswordRequest("SECUREPASSWORD123!", " SECUREPASSWORD123!");
    var violations = validator.validate(newPassword);

    assertEquals(2, violations.size());
    assertEquals("Password requires at least one: [a-z], [A-z], [0-9], [@$!%*?&]",
        violations.iterator().next().getMessage());
  }

}
