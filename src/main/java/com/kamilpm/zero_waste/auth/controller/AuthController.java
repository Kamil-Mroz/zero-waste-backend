package com.kamilpm.zero_waste.auth.controller;

import org.springframework.web.bind.annotation.RestController;

import com.kamilpm.zero_waste.auth.api.AuthUser;
import com.kamilpm.zero_waste.auth.api.AuthenticatedUser;
import com.kamilpm.zero_waste.auth.dto.AuthResponse;
import com.kamilpm.zero_waste.auth.dto.CreatePasswordRequest;
import com.kamilpm.zero_waste.auth.dto.LoginRequest;
import com.kamilpm.zero_waste.auth.dto.SecurityUser;
import com.kamilpm.zero_waste.auth.dto.UpdatePasswordRequest;
import com.kamilpm.zero_waste.auth.entity.RefreshToken;
import com.kamilpm.zero_waste.auth.service.AuthCookieService;
import com.kamilpm.zero_waste.auth.service.AuthService;
import com.kamilpm.zero_waste.auth.service.JwtService;
import com.kamilpm.zero_waste.auth.service.RefreshTokenService;
import com.kamilpm.zero_waste.common.annotation.RateLimit;
import com.kamilpm.zero_waste.common.exception.TokenException;
import com.kamilpm.zero_waste.common.exception.UnauthorizedException;
import com.kamilpm.zero_waste.user.api.UserApi;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;

@RestController
@RequestMapping(path = "/api/v{version}/auth", version = "1")
@RequiredArgsConstructor
public class AuthController {

  private final AuthCookieService authCookieService;
  private final AuthService authService;
  private final JwtService jwtService;
  private final RefreshTokenService refreshTokenService;
  private final UserApi userApi;

  @Value("${app.prod}")
  private boolean isProd;

  @Value("${refresh-token.expiration}")
  private long refreshTokenExpiration;

  @PostMapping(path = "/login")
  public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest loginRequest,
      HttpServletResponse response) {

    Authentication authentication = authService.verify(loginRequest);

    SecurityUser user = (SecurityUser) authentication.getPrincipal();
    AuthenticatedUser authenticatedUser = new AuthenticatedUser(user.getId(), user.getEmail(), user.getNickname(),
        user.getPassword(), user.getRole(), user.isBanActive(), user.getBannedUntil(), user.getJoinedAt());

    AuthResponse authResponse = getAuthResponse(authenticatedUser, response);

    return ResponseEntity.ok(authResponse);
  }

  @PostMapping(path = "/demo")
  public ResponseEntity<AuthResponse> loginDemo(
      HttpServletResponse response) {

    AuthenticatedUser user = userApi.getDemoUser();

    AuthResponse authResponse = getAuthResponse(user, response);

    return ResponseEntity.ok(authResponse);
  }

  private AuthResponse getAuthResponse(AuthenticatedUser user, HttpServletResponse response) {

    String accessToken = jwtService.generateToken(user);
    RefreshToken refresh = refreshTokenService.generateRefreshToken(user);

    String refreshToken = refresh.getToken();

    authCookieService.addRefreshCookie(response, refreshToken);

    AuthUser authUser =  toAuthUser(user);

    return AuthResponse.builder()
        .accessToken(accessToken)
        .user(authUser)
        .build();

  }

  @PostMapping("/refresh")
  public ResponseEntity<AuthResponse> refresh(HttpServletRequest request, HttpServletResponse response) {

    String refreshToken = extractRefreshToken(request)
        .orElseThrow(() -> new TokenException("Refresh token cookie not found"));

    RefreshToken token = refreshTokenService.verifyToken(refreshToken);

    UUID userId = token.getUserId();

    AuthenticatedUser user = userApi.findById(userId);

    if (user.banActive()) {
      refreshTokenService.revokeAllTokens(List.of(user.id()));

      throw new UnauthorizedException("Account suspended");

    }

    String newAccessToken = jwtService.generateToken(user);

    AuthUser authUser =  toAuthUser(user);

    AuthResponse authResponse = AuthResponse.builder()
        .accessToken(newAccessToken)
        .user(authUser)
        .build();
    return ResponseEntity.ok(authResponse);
  }

  private AuthUser toAuthUser(AuthenticatedUser user) {

    return new AuthUser(user.id(), user.nickname(), user.email(), user.banActive(), user.bannedUntil(),
        user.joinedAt(), user.role());
  }

  @RateLimit(action = "logout", limit = 30, window = 1, unit = ChronoUnit.MINUTES)
  @PostMapping("/logout")
  public ResponseEntity<Void> logout(HttpServletRequest request, HttpServletResponse response) {

    extractRefreshToken(request).ifPresent(token -> {
      try {
        refreshTokenService.revokeToken(token);
      } catch (Exception _) {
        //
      }
    });
    authCookieService.clearRefreshCookie(response);

    return ResponseEntity.noContent().build();
  }

  private Optional<String> extractRefreshToken(HttpServletRequest request) {

    Cookie[] cookies = request.getCookies();

    if (cookies == null)
      return Optional.empty();

    return Arrays.stream(cookies)
        .filter(c -> Objects.equals("refreshToken", c.getName()))
        .map(Cookie::getValue)
        .findFirst();
  }

  @RateLimit(action = "create-password", limit = 5, window = 15, unit = ChronoUnit.MINUTES)
  @PostMapping("/password")
  public ResponseEntity<Void> createPassword(@Valid @RequestBody CreatePasswordRequest passwords) {
    authService.handlePasswordCreation(passwords);
    return new ResponseEntity<>(HttpStatus.CREATED);
  }

  @RateLimit(action = "update-password", limit = 5, window = 15, unit = ChronoUnit.MINUTES)
  @PutMapping("/password")
  public ResponseEntity<Void> updatePassword(@Valid @RequestBody UpdatePasswordRequest passwords) {
    authService.handlePasswordUpdate(passwords);
    return ResponseEntity.ok().build();
  }

}
