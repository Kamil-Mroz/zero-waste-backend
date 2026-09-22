package com.kamilpm.zero_waste.common.events;

import java.util.UUID;

import com.kamilpm.zero_waste.common.dto.UserRole;

public record UserRoleChangeEvent(UUID userId, UserRole oldRole, UserRole newRole) {

}
