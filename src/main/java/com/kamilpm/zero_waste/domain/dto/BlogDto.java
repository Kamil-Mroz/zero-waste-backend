package com.kamilpm.zero_waste.domain.dto;

import java.time.Instant;
import java.util.UUID;

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
  private UserDto author;
  private Instant createdAt;
  private Instant updatedAt;
}
