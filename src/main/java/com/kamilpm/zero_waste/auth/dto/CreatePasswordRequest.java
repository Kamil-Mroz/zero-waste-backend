package com.kamilpm.zero_waste.auth.dto;

import com.kamilpm.zero_waste.common.annotation.StrongPassword;

public record CreatePasswordRequest(
    @StrongPassword String newPassword,
    @StrongPassword String confirmPassword) {

}
