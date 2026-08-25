package com.kamilpm.zero_waste.controller;

import org.springframework.web.bind.annotation.RestController;

import com.kamilpm.zero_waste.domain.dto.UserDto;
import com.kamilpm.zero_waste.domain.entity.RefreshToken;
import com.kamilpm.zero_waste.domain.entity.User;
import com.kamilpm.zero_waste.domain.mapper.UserMapper;
import com.kamilpm.zero_waste.domain.request.CreatePasswordRequest;
import com.kamilpm.zero_waste.domain.request.LoginRequest;
import com.kamilpm.zero_waste.domain.request.RegisterRequest;
import com.kamilpm.zero_waste.domain.request.UpdatePasswordRequest;
import com.kamilpm.zero_waste.domain.response.AuthResponse;
import com.kamilpm.zero_waste.exception.TokenException;
import com.kamilpm.zero_waste.exception.UnauthorizedException;
import com.kamilpm.zero_waste.service.AuthCookieService;
import com.kamilpm.zero_waste.service.AuthService;
import com.kamilpm.zero_waste.service.JwtService;
import com.kamilpm.zero_waste.service.RefreshTokenService;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
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
  private final UserMapper userMapper;

  @Value("${app.prod}")
  private boolean isProd;

  @Value("${refresh-token.expiration}")
  private long refreshTokenExpiration;

  @PostMapping(path = "/register")
  public ResponseEntity<UserDto> register(@Valid @RequestBody RegisterRequest registerRequest) {
    throw new UnauthorizedException("Registration disabled");
    // User user = authService.register(registerRequest);
    // return new ResponseEntity<>(userMapper.toDto(user), HttpStatus.CREATED);
  }

  @PostMapping(path = "/login")
  public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest loginRequest,
      HttpServletResponse response) {

    Authentication authentication = authService.verify(loginRequest);

    User user = (User) authentication.getPrincipal();

    AuthResponse authResponse = getAuthResponse(user, response);

    return ResponseEntity.ok(authResponse);
  }

  @PostMapping(path = "/demo")
  public ResponseEntity<AuthResponse> loginDemo(
      HttpServletResponse response) {

    User user = authService.getDemoUser();

    AuthResponse authResponse = getAuthResponse(user, response);

    return ResponseEntity.ok(authResponse);
  }

  private AuthResponse getAuthResponse(User user, HttpServletResponse response) {

    String accessToken = jwtService.generateToken(user);
    RefreshToken refresh = refreshTokenService.generateRefreshToken(user);

    String refreshToken = refresh.getToken();

    authCookieService.addRefreshCookie(response, refreshToken);

    UserDto userDto = userMapper.toDto(refresh.getUser());

    return AuthResponse.builder()
        .accessToken(accessToken)
        .user(userDto)
        .build();

  }

  @PostMapping("/refresh")
  public ResponseEntity<AuthResponse> refresh(HttpServletRequest request, HttpServletResponse response) {

    String refreshToken = extractRefreshToken(request)
        .orElseThrow(() -> new TokenException("Refresh token cookie not found"));

    RefreshToken token = refreshTokenService.verifyToken(refreshToken);

    User user = token.getUser();
    if (user.isBanActive()) {
      refreshTokenService.revokeAllTokens(user.getId());

      throw new UnauthorizedException("Account suspended");

    }

    Authentication authentication = new UsernamePasswordAuthenticationToken(user, null,
        user.getAuthorities());

    String newAccessToken = jwtService.generateToken((User) authentication.getPrincipal());

    AuthResponse authResponse = AuthResponse.builder()
        .accessToken(newAccessToken)
        .user(userMapper.toDto(user))
        .build();
    return ResponseEntity.ok(authResponse);
  }

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

  @PostMapping("/password")
  public ResponseEntity<Void> createPassword(@Valid @RequestBody CreatePasswordRequest passwords) {
    authService.handlePasswordCreation(passwords);
    return new ResponseEntity<>(HttpStatus.CREATED);
  }

  @PutMapping("/password")
  public ResponseEntity<Void> updatePassword(@Valid @RequestBody UpdatePasswordRequest passwords) {
    authService.handlePasswordUpdate(passwords);
    return ResponseEntity.ok().build();
  }

}
