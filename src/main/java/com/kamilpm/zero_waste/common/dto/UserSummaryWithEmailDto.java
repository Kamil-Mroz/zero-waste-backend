package com.kamilpm.zero_waste.common.dto;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserSummaryWithEmailDto {
  private UUID id;
  private String nickname;
  private String email;
}
