package com.kamilpm.zero_waste.controller;

import org.springframework.web.bind.annotation.RestController;

import com.kamilpm.zero_waste.domain.dto.UserDto;
import com.kamilpm.zero_waste.domain.entity.RefreshToken;
import com.kamilpm.zero_waste.domain.entity.User;
import com.kamilpm.zero_waste.domain.entity.UserRole;
import com.kamilpm.zero_waste.domain.mapper.UserMapper;
import com.kamilpm.zero_waste.domain.request.LoginRequest;
import com.kamilpm.zero_waste.domain.request.RegisterRequest;
import com.kamilpm.zero_waste.domain.response.AuthResponse;
import com.kamilpm.zero_waste.exception.TokenException;
import com.kamilpm.zero_waste.security.MyUserDetails;
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
import java.util.Set;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;

@RestController
@RequestMapping(path = "/api/v{version}/auth", version = "1")
@RequiredArgsConstructor
public class AuthController {

  private final AuthService authService;
  private final JwtService jwtService;
  private final RefreshTokenService refreshTokenService;
  private final UserMapper userMapper;

  @Value("${refresh-token.expiration}")
  private long refreshTokenExpiration;

  @PostMapping(path = "/register")
  public ResponseEntity<UserDto> register(@Valid @RequestBody RegisterRequest registerRequest) {

    User user = User.builder()
        .firstName(registerRequest.getFirstName())
        .lastName(registerRequest.getLastName())
        .email(registerRequest.getEmail())
        .password(registerRequest.getPassword())
        .phoneNumber(registerRequest.getPhoneNumber())
        .roles(Set.of(UserRole.USER))
        .bannedUntil(null)
        .banActive(false)
        .build();
    User savedUser = authService.register(user);
    return new ResponseEntity<>(userMapper.toDto(savedUser), HttpStatus.CREATED);
  }

  @PostMapping(path = "/login")
  public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest loginRequest,
      HttpServletResponse response) {

    Authentication authentication = authService.verify(loginRequest);

    String accessToken = jwtService.generateToken(authentication);
    RefreshToken refresh = refreshTokenService.generateRefreshToken(authentication);

    String refreshToken = refresh.getToken();

    Cookie cookie = new Cookie("refreshToken", refreshToken);
    cookie.setHttpOnly(true);
    cookie.setSecure(false);
    cookie.setPath("/");
    cookie.setMaxAge((int) refreshTokenExpiration);

    response.addCookie(cookie);

    UserDto user = userMapper.toDto(refresh.getUser());

    AuthResponse authResponse = AuthResponse.builder()
        .accessToken(accessToken)
        .user(user)
        .build();
    return ResponseEntity.ok(authResponse);
  }

  @PostMapping("/refresh")
  public ResponseEntity<AuthResponse> refresh(HttpServletRequest request) {

    String refreshToken = extractRefreshToken(request)
        .orElseThrow(() -> new TokenException("Refresh token cookie not found"));

    RefreshToken token = refreshTokenService.verifyToken(refreshToken);

    User user = token.getUser();
    UserDetails userDetails = MyUserDetails.buildUserDetails(user);

    Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, null,
        userDetails.getAuthorities());

    String newAccessToken = jwtService.generateToken(authentication);

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

    Cookie cookie = new Cookie("refreshToken", null);
    cookie.setHttpOnly(true);
    cookie.setSecure(false);
    cookie.setPath("/");
    cookie.setMaxAge(0);

    response.addCookie(cookie);

    return ResponseEntity.noContent().build();
  }

  // @GetMapping("/me")
  // public ResponseEntity<CurrentUserDto> getCurrentUser() {
  // User user = authService.getAuthenticatedUser();
  // CurrentUserDto currentUserDto =
  // CurrentUserDto.builder().id(user.getId()).email(user.getEmail())
  // .roles(user.getRoles()).build();
  // return ResponseEntity.ok(currentUserDto);
  // }

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
