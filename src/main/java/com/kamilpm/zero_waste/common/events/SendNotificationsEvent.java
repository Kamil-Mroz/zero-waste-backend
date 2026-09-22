package com.kamilpm.zero_waste.common.events;

import java.util.List;
import java.util.UUID;

import com.kamilpm.zero_waste.common.dto.NotificationRecipient;
import com.kamilpm.zero_waste.common.dto.NotificationReferenceType;
import com.kamilpm.zero_waste.common.dto.NotificationType;

public record SendNotificationsEvent(List<NotificationRecipient> recipients, NotificationType type,
    String title,
    String message,
    UUID referenceId,
    NotificationReferenceType referenceType) {

}
