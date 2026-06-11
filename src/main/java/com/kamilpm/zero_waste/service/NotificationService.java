package com.kamilpm.zero_waste.service;

import java.util.UUID;

import com.kamilpm.zero_waste.domain.entity.Notification;
import com.kamilpm.zero_waste.domain.entity.NotificationType;
import com.kamilpm.zero_waste.domain.entity.User;
import com.kamilpm.zero_waste.domain.request.CursorDirection;
import com.kamilpm.zero_waste.domain.request.CursorRequest;
import com.kamilpm.zero_waste.domain.response.CursorResponse;

public interface NotificationService {

  void sendNotification(User recipient, NotificationType type, String title, String message, UUID referenceId,
      String referenceType);

  long getUnreadCount(UUID userId);

  CursorResponse<Notification> getNotifications(UUID userId, CursorRequest cursor, NotificationType notificationType,

      CursorDirection direction, int limit);

  Notification getNotification(UUID userId, UUID notificationId);

  void markAsRead(UUID notificationsId, UUID userId);

  void markAllAsRead(UUID userId);

}
