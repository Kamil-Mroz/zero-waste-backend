package com.kamilpm.zero_waste.domain.response;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

import com.kamilpm.zero_waste.domain.entity.UserRole;

public record ProfileResponse(
    UUID id,
    String firstName,
    String lastName,
    String email,
    String phoneNumber,
    boolean hasActiveBan,
    Instant bannedUntil,
    Set<UserRole> roles) {
}
