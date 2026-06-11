package com.kamilpm.zero_waste.service;

import java.util.UUID;

import org.springframework.security.core.Authentication;

import com.kamilpm.zero_waste.domain.entity.RefreshToken;

public interface RefreshTokenService {
  RefreshToken generateRefreshToken(Authentication authentication);

  RefreshToken verifyToken(String token);

  void revokeToken(String token);

  void revokeAllTokens(UUID userId);
}
