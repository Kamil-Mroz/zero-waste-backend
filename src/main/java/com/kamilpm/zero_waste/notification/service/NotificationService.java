package com.kamilpm.zero_waste.notification.service;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.data.domain.PageRequest;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Service;

import com.kamilpm.zero_waste.auth.api.AuthApi;
import com.kamilpm.zero_waste.auth.api.AuthenticatedUser;
import com.kamilpm.zero_waste.common.dto.CursorDirection;
import com.kamilpm.zero_waste.common.dto.CursorRequest;
import com.kamilpm.zero_waste.common.dto.CursorResponse;
import com.kamilpm.zero_waste.common.exception.EntityNotFoundException;
import com.kamilpm.zero_waste.notification.api.NotificationRecipient;
import com.kamilpm.zero_waste.notification.api.NotificationReferenceType;
import com.kamilpm.zero_waste.notification.api.NotificationType;
import com.kamilpm.zero_waste.notification.api.SendBanNotificationEvent;
import com.kamilpm.zero_waste.notification.api.SendNotificationEvent;
import com.kamilpm.zero_waste.notification.api.SendNotificationsEvent;
import com.kamilpm.zero_waste.notification.dto.NotificationDto;
import com.kamilpm.zero_waste.notification.dto.NotificationResponse;
import com.kamilpm.zero_waste.notification.entity.Notification;
import com.kamilpm.zero_waste.notification.mapper.NotificationMapper;
import com.kamilpm.zero_waste.notification.repository.NotificationRepository;
import com.kamilpm.zero_waste.user.api.UsersDeletedEvent;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NotificationService {

  private final NotificationRepository notificationRepository;
  private final SimpMessagingTemplate simpMessagingTemplate;
  private final NotificationMapper notificationMapper;
  private final AuthApi authApi;

  private void sendNotification(UUID recipientId, String recipientEmail, NotificationType type, String title,
      String message, UUID referenceId,
      NotificationReferenceType referenceType) {
    Notification notification = Notification.builder()
        .recipientId(recipientId)
        .type(type)
        .title(title)
        .message(message)
        .read(false)
        .referenceId(referenceId)
        .referenceType(referenceType)
        .build();

    Notification savedNotification = notificationRepository.save(notification);

    NotificationResponse payload = new NotificationResponse(savedNotification.getId(), savedNotification.getType(),
        savedNotification.getTitle(), savedNotification.getMessage(),
        savedNotification.isRead(),
        savedNotification.getReferenceId(), savedNotification.getReferenceType(),
        savedNotification.getCreatedAt());

    simpMessagingTemplate.convertAndSendToUser(recipientEmail,
        "/queue/notifications", payload);
  }

  private void sendBanNotification(String userEmail) {
    simpMessagingTemplate.convertAndSendToUser(userEmail,
        "/queue/ban", Map.of("message", "You have been banned"));
  }

  public long getUnreadCount() {

    AuthenticatedUser user = authApi.getRequiredAuthenticatedUser();
    return notificationRepository.countByReadFalseAndRecipientId(user.id());
  }

  public void markAsRead(UUID notificationsId) {

    UUID userId = authApi.getRequiredAuthenticatedUser().id();
    notificationRepository.markAsRead(notificationsId, userId);

  }

  public void markAllAsRead() {
    UUID userId = authApi.getRequiredAuthenticatedUser().id();
    notificationRepository.markAllAsRead(userId);
  }

  public CursorResponse<NotificationDto> getNotifications(CursorRequest cursor,
      NotificationType notificationType,
      CursorDirection direction,
      int limit) {

    List<NotificationDto> notifications;

    UUID userId = authApi.getRequiredAuthenticatedUser().id();

    if (cursor == null) {
      notifications = notificationRepository.findFirstPage(userId, notificationType, PageRequest.of(0, limit + 1))
          .stream().map(notificationMapper::toDto).toList();
    } else {
      if (direction == CursorDirection.FORWARD) {

        notifications = notificationRepository.findOlder(userId, notificationType, cursor.createdAt(), cursor.id(),
            PageRequest.of(0, limit + 1)).stream().map(notificationMapper::toDto).toList();
      } else {

        notifications = notificationRepository.findNewer(userId, notificationType, cursor.createdAt(), cursor.id(),
            PageRequest.of(0, limit + 1)).stream().map(notificationMapper::toDto).toList();

      }
    }

    boolean hasPrev, hasNext;
    if (direction == CursorDirection.BACKWARD) {
      hasPrev = notifications.size() > limit;
      if (hasPrev) {
        notifications.removeLast();
      }
      hasNext = true;
    } else {
      hasNext = notifications.size() > limit;
      if (hasNext) {
        notifications.removeLast();
      }
      hasPrev = cursor != null;
    }

    CursorRequest nextCursor = null;
    if (hasNext && !notifications.isEmpty()) {
      NotificationDto last = notifications.getLast();
      nextCursor = new CursorRequest(last.createdAt(), last.id());
    }

    CursorRequest prevCursor = null;
    if (hasPrev && !notifications.isEmpty()) {
      NotificationDto first = notifications.getFirst();
      prevCursor = new CursorRequest(first.createdAt(), first.id());
    }
    return new CursorResponse<>(notifications, nextCursor, hasNext, prevCursor, hasPrev);
  }

  @Transactional
  public NotificationDto getNotification(UUID notificationId) {
    AuthenticatedUser user = authApi.getRequiredAuthenticatedUser();
    Notification notification = notificationRepository.findByIdAndRecipientId(notificationId, user.id())
        .orElseThrow(() -> new EntityNotFoundException("Notification not found"));
    return notificationMapper.toDto(notification);
  }

  public void deleteAllByUserIds(List<UUID> ids) {
    notificationRepository.deleteByRecipientIdIn(ids);
  }

  @ApplicationModuleListener
  void on(SendNotificationEvent event) {
    sendNotification(event.recipientId(), event.recipientEmail(), event.type(), event.title(), event.message(),
        event.referenceId(),
        event.referenceType());
  }

  @ApplicationModuleListener
  void on(SendNotificationsEvent event) {
    for (NotificationRecipient recepient : event.recipients()) {
      sendNotification(recepient.id(), recepient.email(), event.type(), event.title(), event.message(),
          event.referenceId(),
          event.referenceType());
    }
  }

  @ApplicationModuleListener
  void on(UsersDeletedEvent event) {
    deleteAllByUserIds(event.ids());
  }

  @ApplicationModuleListener
  void on(SendBanNotificationEvent event) {
    sendBanNotification(event.userEmail());
  }

}
