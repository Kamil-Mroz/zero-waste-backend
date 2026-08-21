package com.kamilpm.zero_waste.domain.request;

import com.kamilpm.zero_waste.domain.entity.ReportAction;

import jakarta.annotation.Nonnull;
import jakarta.validation.constraints.NotEmpty;

public record ReportResolveRequest(
    @Nonnull ReportAction reportAction,
    @NotEmpty @Nonnull String adminNote) {

}
