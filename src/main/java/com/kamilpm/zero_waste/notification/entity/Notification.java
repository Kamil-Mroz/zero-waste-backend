package com.kamilpm.zero_waste.notification.entity;

import java.util.UUID;

import com.kamilpm.zero_waste.common.entity.BaseEntity;
import com.kamilpm.zero_waste.notification.api.NotificationReferenceType;
import com.kamilpm.zero_waste.notification.api.NotificationType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
@Table(name = "notifications")
public class Notification extends BaseEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(name = "recipient_id", nullable = false)
  private UUID recipientId;

  @Column(name = "notification_type", nullable = false)
  @Enumerated(EnumType.STRING)
  private NotificationType type;

  @Column(nullable = false)
  private String title;

  @Column(nullable = false)
  private String message;

  @Column(nullable = false)
  private boolean read;

  @Column(name = "reference_id", nullable = false)
  private UUID referenceId;

  @Column(name = "reference_type", nullable = false)
  @Enumerated(EnumType.STRING)
  private NotificationReferenceType referenceType;

}
