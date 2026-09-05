package com.kamilpm.zero_waste.user.api;

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
public class UserDto {
  private UUID id;
  private String nickname;
  private String email;
  private boolean activeBan;
  private Instant bannedUntil;
  private Instant joinedAt;
  private UserRole role;

}
