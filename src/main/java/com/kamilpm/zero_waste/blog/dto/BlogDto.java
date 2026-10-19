package com.kamilpm.zero_waste.blog.dto;

import java.time.Instant;
import java.util.UUID;

import com.kamilpm.zero_waste.common.dto.UserSummaryDto;
import com.kamilpm.zero_waste.common.entity.ModerationStatus;

public record BlogDto(
    UUID id,
    String title,
    String description,
    String content,
    UserSummaryDto author,
    ModerationStatus moderationStatus,
    Instant createdAt,
    Instant updatedAt) {
}
