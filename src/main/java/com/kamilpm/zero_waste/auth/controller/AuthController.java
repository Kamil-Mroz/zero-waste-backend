package com.kamilpm.zero_waste.auth.controller;

import org.springframework.web.bind.annotation.RestController;

import com.kamilpm.zero_waste.auth.dto.AuthResponse;
import com.kamilpm.zero_waste.auth.dto.CreatePasswordRequest;
import com.kamilpm.zero_waste.auth.dto.LoginRequest;
import com.kamilpm.zero_waste.auth.dto.UpdatePasswordRequest;
import com.kamilpm.zero_waste.auth.service.AuthService;
import com.kamilpm.zero_waste.common.annotation.RateLimit;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import java.time.temporal.ChronoUnit;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;

@RestController
@RequestMapping(path = "/api/v{version}/auth", version = "1")
@RequiredArgsConstructor
public class AuthController {

  private final AuthService authService;

  @Value("${app.prod}")
  private boolean isProd;

  @Value("${refresh-token.expiration}")
  private long refreshTokenExpiration;

  @PostMapping(path = "/login")
  public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest loginRequest,
      HttpServletResponse response) {

    AuthResponse authResponse = authService.login(loginRequest, response);

    return ResponseEntity.ok(authResponse);
  }

  @PostMapping(path = "/demo")
  public ResponseEntity<AuthResponse> loginDemo(
      HttpServletResponse response) {

    AuthResponse authResponse = authService.getDemoUser(response);

    return ResponseEntity.ok(authResponse);
  }

  @PostMapping("/refresh")
  public ResponseEntity<AuthResponse> refresh(HttpServletRequest request, HttpServletResponse response) {

    AuthResponse authResponse = authService.handleRefreshToken(request);
    return ResponseEntity.ok(authResponse);
  }

  @RateLimit(action = "logout", limit = 30, window = 1, unit = ChronoUnit.MINUTES)
  @PostMapping("/logout")
  public ResponseEntity<Void> logout(HttpServletRequest request, HttpServletResponse response) {

    authService.logout(request, response);

    return ResponseEntity.noContent().build();
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
