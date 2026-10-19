package com.kamilpm.zero_waste.user.dto;

import java.util.List;
import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

public record UnbanRequest(
    @NotEmpty(message = "At least one user id is required") List<UUID> ids,
    @NotBlank(message = "Reason must be provided") String revokedReason) {
}
