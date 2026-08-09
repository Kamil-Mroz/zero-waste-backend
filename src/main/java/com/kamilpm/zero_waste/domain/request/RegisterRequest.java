package com.kamilpm.zero_waste.domain.request;

import com.kamilpm.zero_waste.annotation.StrongPassword;

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
public class RegisterRequest {
  @NotBlank(message = "Nickname is required")
  private String nickname;
  @NotBlank(message = "Email is required")
  @Email(message = "Must be a valid email")
  private String email;
  @StrongPassword
  private String password;

}
