package com.kamilpm.zero_waste.domain.request;

import java.util.List;
import java.util.UUID;

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

public class UnbanRequest {

  @NotEmpty(message = "At least one user id is required")
  private List<UUID> ids;
  @NotBlank(message = "Reason must be provided")
  private String revokedReason;

}
