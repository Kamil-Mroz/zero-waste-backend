package com.kamilpm.zero_waste.service;

import java.util.UUID;


import com.kamilpm.zero_waste.domain.entity.RefreshToken;
import com.kamilpm.zero_waste.domain.entity.User;

public interface RefreshTokenService {
  RefreshToken generateRefreshToken(User user);

  RefreshToken verifyToken(String token);

  void revokeToken(String token);

  void revokeAllTokens(UUID userId);
}
