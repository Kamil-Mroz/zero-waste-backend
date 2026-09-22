package com.kamilpm.zero_waste.notification.mapper;

import org.springframework.stereotype.Component;

import com.kamilpm.zero_waste.notification.dto.NotificationDto;
import com.kamilpm.zero_waste.notification.entity.Notification;

@Component
public class NotificationMapper {
  public NotificationDto toDto(Notification notification) {
    if (notification == null) {
      return null;
    }
    return new NotificationDto(notification.getId(), notification.getType(), notification.getTitle(),
        notification.getMessage(), notification.isRead(), notification.getReferenceId(),
        notification.getReferenceType(), notification.getCreatedAt());

  };

}
