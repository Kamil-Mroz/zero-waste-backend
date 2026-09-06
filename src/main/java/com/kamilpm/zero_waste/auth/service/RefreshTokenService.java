package com.kamilpm.zero_waste.auth.service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Service;

import com.kamilpm.zero_waste.auth.api.AuthenticatedUser;
import com.kamilpm.zero_waste.auth.api.RevokeRefreshTokenEvent;
import com.kamilpm.zero_waste.auth.entity.RefreshToken;
import com.kamilpm.zero_waste.auth.repository.RefreshTokenRepository;
import com.kamilpm.zero_waste.common.exception.TokenException;
import com.kamilpm.zero_waste.user.api.UsersDeletedEvent;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {
  private final RefreshTokenRepository refreshTokenRepository;

  @Value("${refresh-token.expiration}")
  private long refreshTokenExpiration;

  @ApplicationModuleListener
  void on(RevokeRefreshTokenEvent event) {
    revokeAllTokens(event.ids());
  }

  @ApplicationModuleListener
  void on(UsersDeletedEvent event) {
    refreshTokenRepository.deleteAllByUserIds(event.ids());
  }

  @Transactional
  public RefreshToken generateRefreshToken(AuthenticatedUser user) {
    RefreshToken token = new RefreshToken();
    token.setUserId(user.id());
    token.setToken(UUID.randomUUID().toString());
    token.setExpiryDate(Instant.now().plus(refreshTokenExpiration, ChronoUnit.SECONDS));
    token.setRevoked(false);
    return refreshTokenRepository.save(token);
  }

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

  @Transactional
  public void revokeToken(String token) {
    refreshTokenRepository.findByToken(token).ifPresent(refreshToken -> {
      refreshToken.setRevoked(true);
      refreshTokenRepository.save(refreshToken);
    });
  }

  @Transactional
  public void revokeAllTokens(List<UUID> userId) {
    refreshTokenRepository.revokeAllByUserIds(userId);
  }

}
