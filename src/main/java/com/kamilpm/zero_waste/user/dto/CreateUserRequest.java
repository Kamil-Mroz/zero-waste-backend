package com.kamilpm.zero_waste.user.dto;

import com.kamilpm.zero_waste.common.annotation.StrongPassword;
import com.kamilpm.zero_waste.common.dto.UserRole;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record CreateUserRequest(
    @NotBlank(message = "Nickname is required") String nickname,
    @NotBlank(message = "Email is required") @Email(message = "Must be a valid email") String email,
    @StrongPassword String password,
    UserRole role) {
}
