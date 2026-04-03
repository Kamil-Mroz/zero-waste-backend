package com.kamilpm.zero_waste.controller;

import org.springframework.web.bind.annotation.RestController;

import com.kamilpm.zero_waste.domain.MyUserDetails;
import com.kamilpm.zero_waste.domain.dto.AuthResponse;
import com.kamilpm.zero_waste.domain.dto.LoginRequest;
import com.kamilpm.zero_waste.domain.dto.RegisterRequest;
import com.kamilpm.zero_waste.domain.entity.RefreshToken;
import com.kamilpm.zero_waste.domain.entity.User;
import com.kamilpm.zero_waste.domain.entity.UserRole;
import com.kamilpm.zero_waste.exception.BadCredentialsExceptionCustom;
import com.kamilpm.zero_waste.exception.TokenException;
import com.kamilpm.zero_waste.service.AuthService;
import com.kamilpm.zero_waste.service.JwtService;
import com.kamilpm.zero_waste.service.RefreshTokenService;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;

@RestController
@RequestMapping(path = "/api/v{version}/auth", version = "v1")
@RequiredArgsConstructor
public class AuthController {

  private final AuthService authService;
  private final JwtService jwtService;
  private final RefreshTokenService refreshTokenService;

  @Value("${refresh-token.expiration}")
  private long refreshTokenExpiration;

  @PostMapping(path = "/register")
  public ResponseEntity<User> register(@RequestBody RegisterRequest registerRequest) {

    User user = new User(null, registerRequest.getFirstName(), registerRequest.getLastName(),
        registerRequest.getEmail(), registerRequest.getPassword(),
        registerRequest.getPhoneNumber(), registerRequest.getLocation(), false, new HashSet<UserRole>() {
          {
            add(UserRole.USER);
            add(UserRole.ADMIN);
          }
        });
    User savedUser = authService.register(user);
    return new ResponseEntity<User>(savedUser, HttpStatus.CREATED);
  }

  @PostMapping(path = "/login")
  public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest loginRequest, HttpServletResponse response) {
    UserDetails userDetails;
    try {
      userDetails = (MyUserDetails) authService.verify(loginRequest);
    } catch (BadCredentialsException e) {
      throw new BadCredentialsExceptionCustom("Invalid credentials");
    }

    String accessToken = jwtService.generateToken(userDetails);
    String refreshToken = refreshTokenService.generateRefreshToken(userDetails).getToken();

    Cookie cookie = new Cookie("refreshToken", refreshToken);
    cookie.setHttpOnly(true);
    cookie.setSecure(false);
    cookie.setPath("/");
    cookie.setMaxAge((int) refreshTokenExpiration);

    response.addCookie(cookie);

    AuthResponse authResponse = new AuthResponse(accessToken);
    return ResponseEntity.ok(authResponse);
  }

  @PostMapping("/refresh")
  public ResponseEntity<AuthResponse> refresh(HttpServletRequest request) {

    String refreshToken = extractRefreshToken(request)
        .orElseThrow(() -> new TokenException("Refresh token cookie not found"));

    RefreshToken token = refreshTokenService.verifyToken(refreshToken);

    User user = token.getUser();
    UserDetails userDetails = new MyUserDetails(user);

    String newAccessToken = jwtService.generateToken(userDetails);

    return ResponseEntity.ok(new AuthResponse(newAccessToken));
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

    Cookie cookie = new Cookie("refreshToken", null);
    cookie.setHttpOnly(true);
    cookie.setSecure(false);
    cookie.setPath("/");
    cookie.setMaxAge(0);

    response.addCookie(cookie);

    return ResponseEntity.ok().build();
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

}
