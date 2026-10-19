package com.kamilpm.zero_waste.auth.dto;

public record AuthResponse(
    String accessToken,
    AuthUser user) {

}
