package com.kamilpm.zero_waste.auth.dto;

import java.time.Instant;
import java.util.UUID;


public record AuthenticatedUser(UUID id, String email, String nickname, String password, UserRole role,
    boolean banActive,
    Instant bannedUntil, Instant joinedAt) {

}
