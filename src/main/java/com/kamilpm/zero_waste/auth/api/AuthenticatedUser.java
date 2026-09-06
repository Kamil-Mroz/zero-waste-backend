package com.kamilpm.zero_waste.auth.api;

import java.time.Instant;
import java.util.UUID;

import com.kamilpm.zero_waste.user.api.UserRole;

public record AuthenticatedUser(UUID id, String email, String nickname, String password, UserRole role,
    boolean banActive,
    Instant bannedUntil, Instant joinedAt) {

}
