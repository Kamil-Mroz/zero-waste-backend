package com.kamilpm.zero_waste.blog.dto;

import java.time.Instant;
import java.util.UUID;

import com.kamilpm.zero_waste.common.dto.UserSummaryDto;
import com.kamilpm.zero_waste.common.entity.ModerationStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BlogDto {
  private UUID id;
  private String title;
  private String description;
  private String content;
  private UserSummaryDto author;
  private ModerationStatus moderationStatus;
  private Instant createdAt;
  private Instant updatedAt;
}
