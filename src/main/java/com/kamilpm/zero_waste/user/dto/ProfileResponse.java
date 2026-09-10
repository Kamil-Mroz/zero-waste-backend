package com.kamilpm.zero_waste.user.dto;

import java.time.Instant;
import java.util.UUID;


public record ProfileResponse(
    UUID id,
    String nickname,
    String email,
    String phoneNumber,
    boolean hasActiveBan,
    Instant bannedUntil,
    UserRole role) {
}
