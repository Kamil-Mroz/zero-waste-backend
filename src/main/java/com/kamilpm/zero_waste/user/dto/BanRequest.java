package com.kamilpm.zero_waste.user.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

public record BanRequest(
    @NotEmpty(message = "At least one user id is required") List<UUID> ids,
    @NotBlank(message = "Reason must be provided") String reason,
    Instant expiresAt

) {
  @AssertTrue(message = "Expiration date must be in the future")
  public boolean isExpiresAtValid() {
    return expiresAt == null || expiresAt.isAfter(Instant.now());
  }
}
