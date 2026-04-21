package com.kamilpm.zero_waste.domain.request;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BanRequest {

  @NotEmpty(message = "At least one user id is required")
  private List<UUID> ids;

  @NotBlank(message = "Reason must be provided")
  private String reason;

  private Instant expiresAt;

  @AssertTrue(message = "Expiration date must be in the future")
  public boolean isExpiresAtValid() {
    return expiresAt == null || expiresAt.isAfter(Instant.now());
  }
}
