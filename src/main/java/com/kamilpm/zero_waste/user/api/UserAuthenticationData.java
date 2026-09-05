package com.kamilpm.zero_waste.user.api;

import java.time.Instant;
import java.util.UUID;

public record UserAuthenticationData(
    UUID id,
    String email,
    String nickname,
    String password,
    UserRole role,
    boolean banActive,
    Instant bannedUntil,
    Instant joinedAt) {
}
