package com.kamilpm.zero_waste.service;

import org.springframework.security.core.userdetails.UserDetails;

import com.kamilpm.zero_waste.domain.entity.RefreshToken;
import com.kamilpm.zero_waste.domain.entity.User;

public interface RefreshTokenService {
  RefreshToken generateRefreshToken(UserDetails userDetails);

  RefreshToken verifyToken(String token);

  void revokeToken(String token);

  void revokeAllTokens(User user);
}
