package com.kamilpm.zero_waste.moderation.entity;

import java.time.Instant;
import java.util.UUID;

import com.kamilpm.zero_waste.common.entity.BaseEntity;
import com.kamilpm.zero_waste.moderation.dto.ReportSubjectType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "reports")
public class Report extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(name = "reporter_id", nullable = false)
  private UUID reporterId;

  @Enumerated(EnumType.STRING)
  @Column(name = "subject_type", nullable = false)
  private ReportSubjectType subjectType;

  @Column(name = "subject_id", nullable = false)
  private UUID subjectId;

  @Enumerated(EnumType.STRING)
  @Column(name = "reason", nullable = false)
  private ReportReason reason;

  @Column(name = "comment", columnDefinition = "TEXT")
  private String comment;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false)

  private ReportStatus status = ReportStatus.PENDING;

  @Column(name = "resolved_at")
  private Instant resolvedAt;

  @Column(name = "resolved_by")
  private UUID resolvedBy;

  @Column(name = "admin_note", columnDefinition = "TEXT")
  private String adminNote;

  public Report() {
  }

  public Report(UUID id, UUID reporterId, ReportSubjectType subjectType, UUID subjectId, ReportReason reason,
      String comment, ReportStatus status, Instant resolvedAt, UUID resolvedBy, String adminNote) {
    this.id = id;
    this.reporterId = reporterId;
    this.subjectType = subjectType;
    this.subjectId = subjectId;
    this.reason = reason;
    this.comment = comment;
    this.status = status;
    this.resolvedAt = resolvedAt;
    this.resolvedBy = resolvedBy;
    this.adminNote = adminNote;
  }

  public UUID getId() {
    return id;
  }

  public void setId(UUID id) {
    this.id = id;
  }

  public UUID getReporterId() {
    return reporterId;
  }

  public void setReporterId(UUID reporterId) {
    this.reporterId = reporterId;
  }

  public ReportSubjectType getSubjectType() {
    return subjectType;
  }

  public void setSubjectType(ReportSubjectType subjectType) {
    this.subjectType = subjectType;
  }

  public UUID getSubjectId() {
    return subjectId;
  }

  public void setSubjectId(UUID subjectId) {
    this.subjectId = subjectId;
  }

  public ReportReason getReason() {
    return reason;
  }

  public void setReason(ReportReason reason) {
    this.reason = reason;
  }

  public String getComment() {
    return comment;
  }

  public void setComment(String comment) {
    this.comment = comment;
  }

  public ReportStatus getStatus() {
    return status;
  }

  public void setStatus(ReportStatus status) {
    this.status = status;
  }

  public Instant getResolvedAt() {
    return resolvedAt;
  }

  public void setResolvedAt(Instant resolvedAt) {
    this.resolvedAt = resolvedAt;
  }

  public UUID getResolvedBy() {
    return resolvedBy;
  }

  public void setResolvedBy(UUID resolvedBy) {
    this.resolvedBy = resolvedBy;
  }

  public String getAdminNote() {
    return adminNote;
  }

  public void setAdminNote(String adminNote) {
    this.adminNote = adminNote;
  }

  @Override
  public String toString() {
    return "Report [id=" + id + ", reporterId=" + reporterId + ", subjectType=" + subjectType + ", subjectId="
        + subjectId + ", reason=" + reason + ", comment=" + comment + ", status=" + status + ", resolvedAt="
        + resolvedAt + ", resolvedBy=" + resolvedBy + ", adminNote=" + adminNote + "]";
  }

}
