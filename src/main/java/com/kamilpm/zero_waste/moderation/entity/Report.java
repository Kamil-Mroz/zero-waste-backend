package com.kamilpm.zero_waste.moderation.entity;

import java.time.Instant;
import java.util.UUID;

import com.kamilpm.zero_waste.common.entity.BaseEntity;

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
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Builder
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
  @Builder.Default
  private ReportStatus status = ReportStatus.PENDING;

  @Column(name = "resolved_at")
  private Instant resolvedAt;

  @Column(name = "resolved_by")
  private UUID resolvedBy;

  @Column(name = "admin_note", columnDefinition = "TEXT")
  private String adminNote;

}
