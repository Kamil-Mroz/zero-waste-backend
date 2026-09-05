package com.kamilpm.zero_waste.moderation.dto;

import jakarta.annotation.Nonnull;
import jakarta.validation.constraints.NotEmpty;

public record ReportRejectRequest(
    @NotEmpty @Nonnull String adminNote) {

}
