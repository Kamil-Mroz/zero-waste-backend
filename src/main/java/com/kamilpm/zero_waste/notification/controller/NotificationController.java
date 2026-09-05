package com.kamilpm.zero_waste.notification.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kamilpm.zero_waste.common.annotation.RateLimit;
import com.kamilpm.zero_waste.common.dto.CursorDirection;
import com.kamilpm.zero_waste.common.dto.CursorRequest;
import com.kamilpm.zero_waste.common.dto.CursorResponse;
import com.kamilpm.zero_waste.notification.api.NotificationType;
import com.kamilpm.zero_waste.notification.dto.NotificationDto;
import com.kamilpm.zero_waste.notification.service.NotificationService;

import lombok.RequiredArgsConstructor;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@RestController
@RequestMapping(path = "/api/v{version}/notifications", version = "1")
@RequiredArgsConstructor
public class NotificationController {
  private final NotificationService notificationService;

  @GetMapping("/unread-count")
  public ResponseEntity<?> getUnreadCount() {


    long unreadCount = notificationService.getUnreadCount();

    return ResponseEntity.ok(Map.of("unreadCount", unreadCount));
  }

  @GetMapping("/{id}")
  public ResponseEntity<NotificationDto> getNotification(@PathVariable("id") UUID id) {
    return ResponseEntity.ok(notificationService.getNotification( id));
  }

  @GetMapping
  public ResponseEntity<CursorResponse<NotificationDto>> getNotifications(
      @RequestParam(value = "createdAt", required = false) Instant createdAt,
      @RequestParam(value = "id", required = false) UUID id,
      @RequestParam(value = "direction", required = false) CursorDirection direction,
      @RequestParam(value = "notificationType", required = false) NotificationType notificationType,
      @RequestParam(value = "limit", defaultValue = "20") int limit) {

    CursorRequest cursor = createdAt != null && id != null ? new CursorRequest(createdAt, id) : null;


    CursorResponse<NotificationDto> notifications = notificationService.getNotifications(cursor,
        notificationType,

        direction,
        limit);
    return ResponseEntity.ok(notifications);
  }

  @RateLimit(action = "mark-as-read", limit = 60, window = 1, unit = ChronoUnit.MINUTES)
  @PatchMapping("/{id}/read")
  public ResponseEntity<Void> markAsRead(@PathVariable("id") UUID id) {

    notificationService.markAsRead(id);

    return ResponseEntity.ok().build();
  }

  @RateLimit(action = "mark-all-as-read", limit = 20, window = 1, unit = ChronoUnit.MINUTES)
  @PatchMapping("/read-all")
  public ResponseEntity<Void> markAllAsRead() {

    notificationService.markAllAsRead();

    return ResponseEntity.ok().build();
  }

}
