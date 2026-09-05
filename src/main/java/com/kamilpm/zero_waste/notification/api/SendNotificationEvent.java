package com.kamilpm.zero_waste.notification.api;

import java.util.UUID;

public record SendNotificationEvent(UUID recipientId, String recipientEmail, NotificationType type, String title,
    String message,
    UUID referenceId,
    NotificationReferenceType referenceType) {

}
