package com.kamilpm.zero_waste.auth.service;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.kamilpm.zero_waste.auth.api.CurrentUserApi;
import com.kamilpm.zero_waste.auth.dto.AuthResponse;
import com.kamilpm.zero_waste.auth.dto.AuthUser;
import com.kamilpm.zero_waste.auth.dto.CreatePasswordRequest;
import com.kamilpm.zero_waste.auth.dto.LoginRequest;
import com.kamilpm.zero_waste.auth.dto.SecurityUser;
import com.kamilpm.zero_waste.auth.dto.UpdatePasswordRequest;
import com.kamilpm.zero_waste.auth.entity.RefreshToken;
import com.kamilpm.zero_waste.common.dto.CurrentUser;
import com.kamilpm.zero_waste.common.dto.UserAuthenticationData;
import com.kamilpm.zero_waste.common.exception.BadCredentialsExceptionCustom;
import com.kamilpm.zero_waste.common.exception.ConflictException;
import com.kamilpm.zero_waste.common.exception.ForbiddenException;
import com.kamilpm.zero_waste.common.exception.TokenException;
import com.kamilpm.zero_waste.common.exception.UnauthorizedException;
import com.kamilpm.zero_waste.common.interfaces.CurrentUserProvider;
import com.kamilpm.zero_waste.common.interfaces.UserProvider;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {

  private final AuthCookieService authCookieService;
  private final JwtService jwtService;
  private final RefreshTokenService refreshTokenService;
  private final UserProvider userApi;
  private final PasswordEncoder passwordEncoder;
  private final AuthenticationManager authenticationManager;
  private final CurrentUserProvider currentUser;

  public AuthResponse login(LoginRequest loginRequest, HttpServletResponse response) {

    Authentication authentication = verify(loginRequest);

    SecurityUser user = (SecurityUser) authentication.getPrincipal();
    CurrentUser authenticatedUser = new CurrentUser(user.getId(), user.getEmail(), user.getNickname(),
        user.getPassword(), user.getRole(), user.isBanActive(), user.getBannedUntil(), user.getJoinedAt());

    return getAuthResponse(authenticatedUser, response);
  }

  public AuthResponse getDemoUser(HttpServletResponse response) {
    CurrentUser user = getDemoUser();

    return getAuthResponse(user, response);
  }

  public AuthResponse handleRefreshToken(HttpServletRequest request) {

    String refreshToken = extractRefreshToken(request)
        .orElseThrow(() -> new TokenException("Refresh token cookie not found"));

    RefreshToken token = refreshTokenService.verifyToken(refreshToken);

    UUID userId = token.getUserId();

    CurrentUser user = findById(userId);

    if (user.banActive()) {
      refreshTokenService.revokeAllTokens(List.of(user.id()));

      throw new UnauthorizedException("Account suspended");

    }

    String newAccessToken = jwtService.generateToken(user);

    AuthUser authUser = toAuthUser(user);

    return AuthResponse.builder()
        .accessToken(newAccessToken)
        .user(authUser)
        .build();
  }

  public void logout(HttpServletRequest request, HttpServletResponse response) {

    extractRefreshToken(request).ifPresent(token -> {
      try {
        refreshTokenService.revokeToken(token);
      } catch (Exception _) {
        //
      }
    });
    authCookieService.clearRefreshCookie(response);
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

  private AuthResponse getAuthResponse(CurrentUser user, HttpServletResponse response) {

    String accessToken = jwtService.generateToken(user);
    RefreshToken refresh = refreshTokenService.generateRefreshToken(user);

    String refreshToken = refresh.getToken();

    authCookieService.addRefreshCookie(response, refreshToken);

    AuthUser authUser = toAuthUser(user);

    return AuthResponse.builder()
        .accessToken(accessToken)
        .user(authUser)
        .build();
  }

  private AuthUser toAuthUser(CurrentUser user) {

    return new AuthUser(user.id(), user.nickname(), user.email(), user.banActive(), user.bannedUntil(),
        user.joinedAt(), user.role());
  }

  public Authentication verify(LoginRequest loginRequest) {

    try {
      Authentication authentication = authenticationManager.authenticate(
          new UsernamePasswordAuthenticationToken(loginRequest.getEmail().toLowerCase(), loginRequest.getPassword()));

      return authentication;
    } catch (BadCredentialsException e) {
      throw new BadCredentialsExceptionCustom("Invalid credentials");
    }

  }

  public void handlePasswordCreation(CreatePasswordRequest passwords) {
    CurrentUser user = currentUser.getRequiredAuthenticatedUser();
    if (user.password() != null) {
      throw new ConflictException("Password already set");
    }
    if (!Objects.equals(passwords.newPassword(), passwords.confirmPassword())) {
      throw new ConflictException("Passwords do not match");
    }

    userApi.savePassword(user.id(), passwordEncoder.encode(passwords.newPassword()));
  }

  public void handlePasswordUpdate(UpdatePasswordRequest passwords) {
    CurrentUser user = currentUser.getRequiredAuthenticatedUser();
    if (user.password() == null) {
      throw new ConflictException("To update a password you must set one first");
    }
    if (!passwordEncoder.matches(passwords.currentPassword(), user.password())) {
      throw new ForbiddenException("Password invalid");
    }

    if (passwordEncoder.matches(passwords.newPassword(), user.password())) {
      throw new ConflictException("New password must be different from the current password");
    }
    if (!Objects.equals(passwords.newPassword(), passwords.confirmPassword())) {
      throw new ConflictException("Passwords do not match");
    }

    userApi.savePassword(user.id(), passwordEncoder.encode(passwords.newPassword()));

  }

  private CurrentUser getDemoUser() {

    UserAuthenticationData user = userApi.getDemoUser();
    return new CurrentUser(user.id(), user.email(), user.nickname(), user.password(),
        user.role(), user.banActive(), user.bannedUntil(), user.joinedAt());
  }

  public CurrentUser findById(UUID userId) {
    UserAuthenticationData user = userApi.findById(userId);
    return new CurrentUser(user.id(), user.email(), user.nickname(), user.password(),
        user.role(), user.banActive(), user.bannedUntil(), user.joinedAt());
  }

}
