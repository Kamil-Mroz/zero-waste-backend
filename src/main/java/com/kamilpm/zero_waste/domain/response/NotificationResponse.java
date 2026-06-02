package com.kamilpm.zero_waste.domain.response;

import java.time.Instant;
import java.util.UUID;

import com.kamilpm.zero_waste.domain.entity.NotificationType;

public record NotificationResponse(UUID id, NotificationType type, String title, String message, boolean read,
    UUID referenceId, String referenceType, Instant createdAt) {

}
