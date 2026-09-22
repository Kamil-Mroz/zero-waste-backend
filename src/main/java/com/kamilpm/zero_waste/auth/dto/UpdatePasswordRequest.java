package com.kamilpm.zero_waste.auth.dto;

import com.kamilpm.zero_waste.common.annotation.StrongPassword;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

public record UpdatePasswordRequest(
    @NotEmpty @NotBlank String currentPassword,
    @StrongPassword String newPassword,
    @StrongPassword String confirmPassword) {

}
