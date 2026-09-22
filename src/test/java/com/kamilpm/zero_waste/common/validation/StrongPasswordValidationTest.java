package com.kamilpm.zero_waste.common.validation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.kamilpm.zero_waste.auth.dto.CreatePasswordRequest;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

import static org.assertj.core.api.Assertions.assertThat;

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

    assertThat(violations.isEmpty()).isTrue();
  }

  @Test
  void validStrongPasswordShouldFailNoSpecialCharacter() {
    var newPassword = new CreatePasswordRequest("SecurePassword123", " SecurePassword123");
    var violations = validator.validate(newPassword);

    assertThat(violations.size()).isEqualTo(2);
    assertThat(
        violations.iterator().next().getMessage())
        .isEqualTo("Password requires at least one: [a-z], [A-z], [0-9], [@$!%*?&]");
  }

  @Test
  void validStrongPasswordShouldFailNoNumber() {
    var newPassword = new CreatePasswordRequest("SecurePassword!", " SecurePassword!");
    var violations = validator.validate(newPassword);

    assertThat(violations.size()).isEqualTo(2);
    assertThat(
        violations.iterator().next().getMessage())
        .isEqualTo("Password requires at least one: [a-z], [A-z], [0-9], [@$!%*?&]");
  }

  @Test
  void validStrongPasswordShouldFailNoUppercase() {
    var newPassword = new CreatePasswordRequest("securepassword123!", " securepassword123!");
    var violations = validator.validate(newPassword);

    assertThat(violations.size()).isEqualTo(2);
    assertThat(
        violations.iterator().next().getMessage())
        .isEqualTo("Password requires at least one: [a-z], [A-z], [0-9], [@$!%*?&]");
  }

  @Test
  void validStrongPasswordShouldFailNoLowercase() {
    var newPassword = new CreatePasswordRequest("SECUREPASSWORD123!", " SECUREPASSWORD123!");
    var violations = validator.validate(newPassword);

    assertThat(violations.size()).isEqualTo(2);
    assertThat(
        violations.iterator().next().getMessage())
        .isEqualTo("Password requires at least one: [a-z], [A-z], [0-9], [@$!%*?&]");
  }

}
