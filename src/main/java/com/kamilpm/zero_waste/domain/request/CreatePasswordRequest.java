package com.kamilpm.zero_waste.domain.request;

import com.kamilpm.zero_waste.annotation.StrongPassword;

public record CreatePasswordRequest(
    @StrongPassword String newPassword,
    @StrongPassword String confirmPassword) {

}
