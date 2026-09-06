package com.kamilpm.zero_waste.auth.dto;

import com.kamilpm.zero_waste.auth.api.AuthUser;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthResponse {
  private String accessToken;
  private AuthUser user;

}
