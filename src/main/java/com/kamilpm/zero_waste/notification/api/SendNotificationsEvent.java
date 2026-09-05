package com.kamilpm.zero_waste.notification.api;

import java.util.List;
import java.util.UUID;

public record SendNotificationsEvent(List<NotificationRecipient> recipients, NotificationType type,
    String title,
    String message,
    UUID referenceId,
    NotificationReferenceType referenceType) {

}
