package com.kamilpm.zero_waste.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kamilpm.zero_waste.domain.dto.NotificationDto;
import com.kamilpm.zero_waste.domain.entity.Notification;
import com.kamilpm.zero_waste.domain.entity.NotificationType;
import com.kamilpm.zero_waste.domain.request.CursorDirection;
import com.kamilpm.zero_waste.domain.request.CursorRequest;
import com.kamilpm.zero_waste.domain.response.CursorResponse;
import com.kamilpm.zero_waste.security.MyUserDetails;
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

    MyUserDetails myUserDetails = authService.getRequiredAuthenticatedUserDetails();

    long unreadCount = notificationService.getUnreadCount(myUserDetails.getId());

    return ResponseEntity.ok(Map.of("unreadCount", unreadCount));
  }

  @GetMapping("/{id}")
  public ResponseEntity<NotificationDto> getNotification(@PathVariable UUID id) {
    MyUserDetails user = authService.getRequiredAuthenticatedUserDetails();
    return ResponseEntity.ok(notificationService.getNotification(user.getId(), id));
  }

  @GetMapping()
  public ResponseEntity<CursorResponse<NotificationDto>> getNotifications(
      @RequestParam(required = false) Instant createdAt,
      @RequestParam(required = false) UUID id,
      @RequestParam(required = false) CursorDirection direction,
      @RequestParam(required = false) NotificationType notificationType,
      @RequestParam(defaultValue = "20") int limit) {

    CursorRequest cursor = createdAt != null && id != null ? new CursorRequest(createdAt, id) : null;

    UUID userId = authService.getRequiredAuthenticatedUserDetails().getId();

    CursorResponse<NotificationDto> notifications = notificationService.getNotifications(userId, cursor,
        notificationType,

        direction,
        limit);
    return ResponseEntity.ok(notifications);
  }

  @PatchMapping("/{id}/read")
  public ResponseEntity<Void> markAsRead(@PathVariable UUID id) {

    MyUserDetails myUserDetails = authService.getRequiredAuthenticatedUserDetails();
    notificationService.markAsRead(id, myUserDetails.getId());

    return ResponseEntity.ok().build();
  }

  @PatchMapping("/read-all")
  public ResponseEntity<Void> markAllAsRead() {

    MyUserDetails myUserDetails = authService.getRequiredAuthenticatedUserDetails();
    notificationService.markAllAsRead(myUserDetails.getId());

    return ResponseEntity.ok().build();
  }

}
