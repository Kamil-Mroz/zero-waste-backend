package com.kamilpm.zero_waste.moderation.dto;

import com.kamilpm.zero_waste.moderation.entity.ReportAction;

import jakarta.annotation.Nonnull;
import jakarta.validation.constraints.NotEmpty;

public record ReportResolveRequest(
    @Nonnull ReportAction reportAction,
    @NotEmpty @Nonnull String adminNote) {

}
