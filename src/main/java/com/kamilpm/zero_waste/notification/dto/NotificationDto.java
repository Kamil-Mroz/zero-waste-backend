package com.kamilpm.zero_waste.notification.dto;

import java.time.Instant;
import java.util.UUID;

import com.kamilpm.zero_waste.notification.api.NotificationReferenceType;
import com.kamilpm.zero_waste.notification.api.NotificationType;

public record NotificationDto(UUID id, NotificationType type, String title, String message, boolean read,
    UUID referenceId, NotificationReferenceType referenceType,
    Instant createdAt) {

}
