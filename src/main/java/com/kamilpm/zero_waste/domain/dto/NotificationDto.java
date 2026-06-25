package com.kamilpm.zero_waste.domain.dto;

import java.time.Instant;
import java.util.UUID;

import com.kamilpm.zero_waste.domain.entity.NotificationType;

public record NotificationDto(UUID id, NotificationType type, String title, String message, boolean read,
    UUID referenceId, String referenceType,
    Instant createdAt) {

}
