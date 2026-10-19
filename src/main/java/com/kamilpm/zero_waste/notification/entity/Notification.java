package com.kamilpm.zero_waste.notification.entity;

import java.util.UUID;

import com.kamilpm.zero_waste.common.dto.NotificationReferenceType;
import com.kamilpm.zero_waste.common.dto.NotificationType;
import com.kamilpm.zero_waste.common.entity.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
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

  public Notification() {
  }

  public Notification(UUID id, UUID recipientId, NotificationType type, String title, String message, boolean read,
      UUID referenceId, NotificationReferenceType referenceType) {
    this.id = id;
    this.recipientId = recipientId;
    this.type = type;
    this.title = title;
    this.message = message;
    this.read = read;
    this.referenceId = referenceId;
    this.referenceType = referenceType;
  }

  public UUID getId() {
    return id;
  }

  public void setId(UUID id) {
    this.id = id;
  }

  public UUID getRecipientId() {
    return recipientId;
  }

  public void setRecipientId(UUID recipientId) {
    this.recipientId = recipientId;
  }

  public NotificationType getType() {
    return type;
  }

  public void setType(NotificationType type) {
    this.type = type;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  public boolean isRead() {
    return read;
  }

  public void setRead(boolean read) {
    this.read = read;
  }

  public UUID getReferenceId() {
    return referenceId;
  }

  public void setReferenceId(UUID referenceId) {
    this.referenceId = referenceId;
  }

  public NotificationReferenceType getReferenceType() {
    return referenceType;
  }

  public void setReferenceType(NotificationReferenceType referenceType) {
    this.referenceType = referenceType;
  }

  @Override
  public String toString() {
    return "Notification [id=" + id + ", recipientId=" + recipientId + ", type=" + type + ", title=" + title
        + ", message=" + message + ", read=" + read + ", referenceId=" + referenceId + ", referenceType="
        + referenceType + "]";
  }

}
