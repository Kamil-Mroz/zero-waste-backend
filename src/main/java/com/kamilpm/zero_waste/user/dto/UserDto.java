package com.kamilpm.zero_waste.user.dto;

import java.time.Instant;
import java.util.UUID;

import com.kamilpm.zero_waste.common.dto.UserRole;

public record UserDto(
    UUID id,
    String nickname,
    String email,
    boolean activeBan,
    Instant bannedUntil,
    Instant joinedAt,
    UserRole role) {

}
