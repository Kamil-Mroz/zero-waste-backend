package com.kamilpm.zero_waste.notification.service;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.data.domain.PageRequest;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Service;

import com.kamilpm.zero_waste.common.dto.CurrentUser;
import com.kamilpm.zero_waste.common.dto.CursorDirection;
import com.kamilpm.zero_waste.common.dto.CursorRequest;
import com.kamilpm.zero_waste.common.dto.CursorResponse;
import com.kamilpm.zero_waste.common.dto.NotificationRecipient;
import com.kamilpm.zero_waste.common.dto.NotificationReferenceType;
import com.kamilpm.zero_waste.common.dto.NotificationType;
import com.kamilpm.zero_waste.common.events.SendBanNotificationEvent;
import com.kamilpm.zero_waste.common.events.SendBansNotificationEvent;
import com.kamilpm.zero_waste.common.events.SendNotificationEvent;
import com.kamilpm.zero_waste.common.events.SendNotificationsEvent;
import com.kamilpm.zero_waste.common.events.SendReportNotificationEvent;
import com.kamilpm.zero_waste.common.exception.EntityNotFoundException;
import com.kamilpm.zero_waste.common.interfaces.CurrentUserProvider;
import com.kamilpm.zero_waste.notification.dto.NotificationDto;
import com.kamilpm.zero_waste.notification.dto.NotificationResponse;
import com.kamilpm.zero_waste.notification.entity.Notification;
import com.kamilpm.zero_waste.notification.mapper.NotificationMapper;
import com.kamilpm.zero_waste.notification.repository.NotificationRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NotificationService {

  private final NotificationRepository notificationRepository;
  private final NotificationMapper notificationMapper;
  private final CurrentUserProvider currentUser;
  private final SimpMessagingTemplate simpMessagingTemplate;

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

    CurrentUser user = currentUser.getRequiredAuthenticatedUser();
    return notificationRepository.countByReadFalseAndRecipientId(user.id());
  }

  public void markAsRead(UUID notificationsId) {

    UUID userId = currentUser.getRequiredAuthenticatedUser().id();
    notificationRepository.markAsRead(notificationsId, userId);

  }

  public void markAllAsRead() {
    UUID userId = currentUser.getRequiredAuthenticatedUser().id();
    notificationRepository.markAllAsRead(userId);
  }

  public CursorResponse<NotificationDto> getNotifications(CursorRequest cursor,
      NotificationType notificationType,
      CursorDirection direction,
      int limit) {

    List<NotificationDto> notifications;

    UUID userId = currentUser.getRequiredAuthenticatedUser().id();

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
    CurrentUser user = currentUser.getRequiredAuthenticatedUser();
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
  void on(SendBanNotificationEvent event) {
    sendBanNotification(event.userEmail());
  }

  @ApplicationModuleListener
  void on(SendReportNotificationEvent event) {

    simpMessagingTemplate.convertAndSend("/topic/reports",
        new SendReportNotificationEvent(event.subjectType(), event.comment()));
  }

  @ApplicationModuleListener
  void on(SendBansNotificationEvent event) {
    for (String userEmail : event.usersEmail()) {
      simpMessagingTemplate.convertAndSendToUser(userEmail,
          "/queue/ban", Map.of("message", "You have been banned"));
    }

  }

}
