package com.kamilpm.zero_waste.user.dto;

import com.kamilpm.zero_waste.common.annotation.StrongPassword;
import com.kamilpm.zero_waste.user.api.UserRole;

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
  @NotBlank(message = "Nickname is required")
  private String nickname;
  @NotBlank(message = "Email is required")
  @Email(message = "Must be a valid email")
  private String email;
  @StrongPassword
  private String password;
  private UserRole role;
}
