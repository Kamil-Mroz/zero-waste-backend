package com.kamilpm.zero_waste.validation;

import java.util.regex.Pattern;

import com.kamilpm.zero_waste.annotation.NullablePassword;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class NullablePasswordValidator implements ConstraintValidator<NullablePassword, String> {
  private static final Pattern PATTERN = Pattern
      .compile("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]+$");

  @Override
  public boolean isValid(String value, ConstraintValidatorContext context) {
    if (value == null || value.isBlank()) {
      return true;
    }

    return value.length() >= 10 && value.length() <= 128 && PATTERN.matcher(value).matches();
  }

}
