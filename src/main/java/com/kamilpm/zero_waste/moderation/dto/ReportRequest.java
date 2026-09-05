package com.kamilpm.zero_waste.moderation.dto;

import java.util.UUID;

import com.kamilpm.zero_waste.moderation.entity.ReportReason;
import com.kamilpm.zero_waste.moderation.entity.ReportSubjectType;

import jakarta.annotation.Nonnull;
import lombok.NonNull;

public record ReportRequest(
    @Nonnull ReportSubjectType subjectType,
    @NonNull UUID subjectId,
    @NonNull ReportReason reason,
    String comment) {

}
