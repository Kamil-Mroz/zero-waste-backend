package com.kamilpm.zero_waste.domain.dto;

import java.time.Instant;
import java.util.UUID;

import com.kamilpm.zero_waste.domain.entity.ReportReason;
import com.kamilpm.zero_waste.domain.entity.ReportStatus;
import com.kamilpm.zero_waste.domain.entity.ReportSubjectType;

public record ReportDto(
    UUID id,
    UserSummaryDto reporter,
    ReportSubjectType subjectType,
    UUID subjectId,
    ReportReason reason,
    String comment,
    ReportStatus status,
    Instant resolvedAt,
    UserSummaryDto resolvedBy,
    String adminNote) {

}
