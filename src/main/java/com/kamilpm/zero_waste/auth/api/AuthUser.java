package com.kamilpm.zero_waste.auth.api;

import java.time.Instant;
import java.util.UUID;

import com.kamilpm.zero_waste.user.api.UserRole;

public record AuthUser(
    UUID id,
    String nickname,
    String email,
    boolean activeBan,
    Instant bannedUntil,
    Instant joinedAt,
    UserRole role) {

}
