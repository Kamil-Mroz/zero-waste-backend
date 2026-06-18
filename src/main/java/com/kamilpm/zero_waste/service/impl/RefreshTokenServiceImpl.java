package com.kamilpm.zero_waste.service.impl;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import com.kamilpm.zero_waste.domain.entity.RefreshToken;
import com.kamilpm.zero_waste.domain.entity.User;
import com.kamilpm.zero_waste.exception.EntityNotFoundException;
import com.kamilpm.zero_waste.exception.TokenException;
import com.kamilpm.zero_waste.repository.RefreshTokenRepository;
import com.kamilpm.zero_waste.repository.UserRepository;
import com.kamilpm.zero_waste.security.MyUserDetails;
import com.kamilpm.zero_waste.service.RefreshTokenService;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RefreshTokenServiceImpl implements RefreshTokenService {
  private final RefreshTokenRepository refreshTokenRepository;
  private final UserRepository userRepository;

  @Value("${refresh-token.expiration}")
  private long refreshTokenExpiration;

  @Override
  @Transactional
  public RefreshToken generateRefreshToken(Authentication authentication) {
    MyUserDetails userDetails = (MyUserDetails) authentication.getPrincipal();
    User user = userRepository.findById(userDetails.getId())
        .orElseThrow(() -> new EntityNotFoundException("User not found"));

    RefreshToken token = new RefreshToken();
    token.setUser(user);
    token.setToken(UUID.randomUUID().toString());
    token.setExpiryDate(Instant.now().plus(refreshTokenExpiration, ChronoUnit.SECONDS));
    token.setRevoked(false);

    return refreshTokenRepository.save(token);
  }

  @Override
  public RefreshToken verifyToken(String token) {
    RefreshToken refreshToken = refreshTokenRepository.findByToken(token)
        .orElseThrow(() -> new TokenException("Invalid refresh token"));

    if (refreshToken.isRevoked()) {
      throw new TokenException("Refresh token revoked");
    }
    if (refreshToken.getExpiryDate().isBefore(Instant.now())) {
      throw new TokenException("Refresh token expired");
    }

    return refreshToken;
  }

  @Override
  @Transactional
  public void revokeToken(String token) {
    refreshTokenRepository.findByToken(token).ifPresent(refreshToken -> {
      refreshToken.setRevoked(true);
      refreshTokenRepository.save(refreshToken);
    });
  }

  @Override
  @Transactional
  public void revokeAllTokens(UUID userId) {
    refreshTokenRepository.revokeAllByUserId(userId);
  }

}
