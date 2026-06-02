package com.kamilpm.zero_waste.service.impl;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.PageRequest;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import com.kamilpm.zero_waste.domain.entity.Notification;
import com.kamilpm.zero_waste.domain.entity.NotificationType;
import com.kamilpm.zero_waste.domain.entity.User;
import com.kamilpm.zero_waste.domain.request.CursorDirection;
import com.kamilpm.zero_waste.domain.request.CursorRequest;
import com.kamilpm.zero_waste.domain.response.CursorResponse;
import com.kamilpm.zero_waste.domain.response.NotificationResponse;
import com.kamilpm.zero_waste.repository.NotificationRepository;
import com.kamilpm.zero_waste.service.NotificationService;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

  private final NotificationRepository notificationRepository;
  private final SimpMessagingTemplate simpMessagingTemplate;

  @Override
  public void sendNotification(User recipient, NotificationType type, String title, String message, UUID referenceId,
      String referenceType) {
    Notification notification = Notification.builder()
        .recipient(recipient)
        .type(type)
        .title(title)
        .message(message)
        .read(false)
        .referenceId(referenceId)
        .referenceType(referenceType)
        .build();

    Notification savedNotification = notificationRepository.save(notification);

    NotificationResponse payload = new NotificationResponse(savedNotification.getId(), savedNotification.getType(),
        savedNotification.getTitle(), savedNotification.getMessage(), savedNotification.isRead(),
        savedNotification.getReferenceId(), savedNotification.getReferenceType(), savedNotification.getCreatedAt());

    simpMessagingTemplate.convertAndSendToUser(recipient.getEmail(), "/queue/notifications", payload);
  }

  @Override
  public long getUnreadCount(UUID userId) {

    return notificationRepository.countByReadFalseAndRecipientId(userId);
  }

  @Override
  public void markAsRead(UUID notificationsId, UUID userId) {
    notificationRepository.markAsRead(notificationsId, userId);

  }

  @Override
  public void markAllAsRead(UUID userId) {
    notificationRepository.markAllAsRead(userId);
  }

  @Override
  public CursorResponse<Notification> getNotifications(UUID userId, CursorRequest cursor,
      NotificationType notificationType,
      CursorDirection direction,
      int limit) {

    List<Notification> notifications;

    if (cursor == null) {
      notifications = notificationRepository.findFirstPage(userId, notificationType, PageRequest.of(0, limit + 1));
    } else {
      if (direction == CursorDirection.FORWARD) {

        notifications = notificationRepository.findOlder(userId, notificationType, cursor.createdAt(), cursor.id(),
            PageRequest.of(0, limit + 1));
      } else {

        notifications = notificationRepository.findNewer(userId, notificationType, cursor.createdAt(), cursor.id(),
            PageRequest.of(0, limit + 1));

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
      Notification last = notifications.getLast();
      nextCursor = new CursorRequest(last.getCreatedAt(), last.getId());
    }

    CursorRequest prevCursor = null;
    if (hasPrev && !notifications.isEmpty()) {
      Notification first = notifications.getFirst();
      prevCursor = new CursorRequest(first.getCreatedAt(), first.getId());
    }
    return new CursorResponse<>(notifications, nextCursor, hasNext, prevCursor, hasPrev);
  }

  @Override
  public Notification getNotification(UUID userId, UUID notificationId) {
    return notificationRepository.findByIdAndRecipient_Id(notificationId, userId)
        .orElseThrow(() -> new EntityNotFoundException("Notification not found"));
  }
}
