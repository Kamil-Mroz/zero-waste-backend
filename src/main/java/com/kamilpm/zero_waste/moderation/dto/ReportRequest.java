package com.kamilpm.zero_waste.moderation.dto;

import java.util.UUID;

import com.kamilpm.zero_waste.moderation.entity.ReportReason;

import jakarta.annotation.Nonnull;

public record ReportRequest(
    @Nonnull ReportSubjectType subjectType,
    @Nonnull UUID subjectId,
    @Nonnull ReportReason reason,
    String comment) {

}
