package com.kamilpm.zero_waste.domain.request;

import jakarta.annotation.Nonnull;
import jakarta.validation.constraints.NotEmpty;

public record ReportRejectRequest(
    @NotEmpty @Nonnull String adminNote) {

}
