package com.kamilpm.zero_waste.moderation.dto;

import java.time.Instant;
import java.util.UUID;

import com.kamilpm.zero_waste.common.dto.UserSummaryDto;
import com.kamilpm.zero_waste.moderation.entity.ReportReason;
import com.kamilpm.zero_waste.moderation.entity.ReportStatus;
import com.kamilpm.zero_waste.moderation.entity.ReportSubjectType;

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
