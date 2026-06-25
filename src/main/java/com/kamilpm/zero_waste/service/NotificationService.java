package com.kamilpm.zero_waste.service;

import java.util.List;
import java.util.UUID;

import com.kamilpm.zero_waste.domain.dto.NotificationDto;
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

  CursorResponse<NotificationDto> getNotifications(UUID userId, CursorRequest cursor, NotificationType notificationType,

      CursorDirection direction, int limit);

  NotificationDto getNotification(UUID userId, UUID notificationId);

  void markAsRead(UUID notificationsId, UUID userId);

  void markAllAsRead(UUID userId);

  void deleteAllByUserIds(List<UUID> ids);

}
