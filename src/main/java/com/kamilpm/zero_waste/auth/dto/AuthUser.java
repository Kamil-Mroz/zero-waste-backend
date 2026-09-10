package com.kamilpm.zero_waste.auth.dto;

import java.time.Instant;
import java.util.UUID;

public record AuthUser(
    UUID id,
    String nickname,
    String email,
    boolean activeBan,
    Instant bannedUntil,
    Instant joinedAt,
    UserRole role) {

}
