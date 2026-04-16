package com.kamilpm.zero_waste.domain.response;

import com.kamilpm.zero_waste.domain.dto.UserDto;

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
  private UserDto user;

}
