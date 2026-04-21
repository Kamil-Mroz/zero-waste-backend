package com.kamilpm.zero_waste.domain.request;

import java.util.Set;

import com.kamilpm.zero_waste.annotation.InternationalPhoneNumber;
import com.kamilpm.zero_waste.annotation.StrongPassword;
import com.kamilpm.zero_waste.domain.entity.UserRole;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateUserRequest {
  @NotBlank(message = "First name is required")
  private String firstName;
  @NotBlank(message = "Last name is required")
  private String lastName;
  @NotBlank(message = "Email is required")
  @Email(message = "Must be a valid email")
  private String email;
  @StrongPassword
  private String password;
  @InternationalPhoneNumber
  private String phoneNumber;
  private Set<UserRole> roles;
}
