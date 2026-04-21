package com.kamilpm.zero_waste.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.kamilpm.zero_waste.validation.NullablePasswordValidator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

@Target({ ElementType.FIELD, ElementType.PARAMETER })
@Constraint(validatedBy = { NullablePasswordValidator.class })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface NullablePassword {
  String message() default "Password requires at least one: [a-z], [A-z], [0-9], [@$!%*?&] or must not be provided";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};

}
