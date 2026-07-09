package com.kamilpm.zero_waste.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kamilpm.zero_waste.domain.dto.NotificationDto;
import com.kamilpm.zero_waste.domain.entity.NotificationType;
import com.kamilpm.zero_waste.domain.entity.User;
import com.kamilpm.zero_waste.domain.request.CursorDirection;
import com.kamilpm.zero_waste.domain.request.CursorRequest;
import com.kamilpm.zero_waste.domain.response.CursorResponse;
import com.kamilpm.zero_waste.service.AuthService;
import com.kamilpm.zero_waste.service.NotificationService;

import lombok.RequiredArgsConstructor;

import java.time.Instant;
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
  private final AuthService authService;

  @GetMapping("/unread-count")
  public ResponseEntity<?> getUnreadCount() {

    User myUserDetails = authService.getRequiredAuthenticatedUser();

    long unreadCount = notificationService.getUnreadCount(myUserDetails.getId());

    return ResponseEntity.ok(Map.of("unreadCount", unreadCount));
  }

  @GetMapping("/{id}")
  public ResponseEntity<NotificationDto> getNotification(@PathVariable("id") UUID id) {
    User user = authService.getRequiredAuthenticatedUser();
    return ResponseEntity.ok(notificationService.getNotification(user.getId(), id));
  }

  @GetMapping
  public ResponseEntity<CursorResponse<NotificationDto>> getNotifications(
      @RequestParam(value = "createdAt", required = false) Instant createdAt,
      @RequestParam(value = "id", required = false) UUID id,
      @RequestParam(value = "direction", required = false) CursorDirection direction,
      @RequestParam(value = "notificationType", required = false) NotificationType notificationType,
      @RequestParam(value = "limit", defaultValue = "20") int limit) {

    CursorRequest cursor = createdAt != null && id != null ? new CursorRequest(createdAt, id) : null;

    UUID userId = authService.getRequiredAuthenticatedUser().getId();

    CursorResponse<NotificationDto> notifications = notificationService.getNotifications(userId, cursor,
        notificationType,

        direction,
        limit);
    return ResponseEntity.ok(notifications);
  }

  @PatchMapping("/{id}/read")
  public ResponseEntity<Void> markAsRead(@PathVariable("id") UUID id) {

    User myUserDetails = authService.getRequiredAuthenticatedUser();
    notificationService.markAsRead(id, myUserDetails.getId());

    return ResponseEntity.ok().build();
  }

  @PatchMapping("/read-all")
  public ResponseEntity<Void> markAllAsRead() {

    User myUserDetails = authService.getRequiredAuthenticatedUser();
    notificationService.markAllAsRead(myUserDetails.getId());

    return ResponseEntity.ok().build();
  }

}
