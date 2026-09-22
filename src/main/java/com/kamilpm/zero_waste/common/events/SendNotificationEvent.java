package com.kamilpm.zero_waste.common.events;

import java.util.UUID;

import com.kamilpm.zero_waste.common.dto.NotificationReferenceType;
import com.kamilpm.zero_waste.common.dto.NotificationType;

public record SendNotificationEvent(UUID recipientId, String recipientEmail, NotificationType type, String title,
    String message,
    UUID referenceId,
    NotificationReferenceType referenceType) {

}
