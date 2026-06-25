package com.kamilpm.zero_waste.service.impl;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.kamilpm.zero_waste.domain.entity.User;
import com.kamilpm.zero_waste.domain.entity.UserRole;
import com.kamilpm.zero_waste.domain.request.LoginRequest;
import com.kamilpm.zero_waste.domain.request.RegisterRequest;
import com.kamilpm.zero_waste.exception.BadCredentialsExceptionCustom;
import com.kamilpm.zero_waste.exception.ConflictException;
import com.kamilpm.zero_waste.exception.ForbiddenException;
import com.kamilpm.zero_waste.exception.UnauthorizedException;
import com.kamilpm.zero_waste.repository.UserRepository;
import com.kamilpm.zero_waste.service.AuthService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final AuthenticationManager authenticationManager;

  @Override
  public User register(RegisterRequest registerRequest) {

    if (userRepository.existsByEmail(registerRequest.getEmail())) {
      throw new ConflictException("Email already in use", "email");
    }
    User user = User.builder()
        .firstName(registerRequest.getFirstName())
        .lastName(registerRequest.getLastName())
        .email(registerRequest.getEmail())
        .password(passwordEncoder.encode(registerRequest.getPassword()))
        .phoneNumber(registerRequest.getPhoneNumber())
        .roles(Set.of(UserRole.USER))
        .bannedUntil(null)
        .banActive(false)
        .build();

    return userRepository.save(user);
  }

  @Override
  public Authentication verify(LoginRequest loginRequest) {

    try {
      Authentication authentication = authenticationManager.authenticate(
          new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword()));

      return authentication;
    } catch (BadCredentialsException e) {
      throw new BadCredentialsExceptionCustom("Invalid credentials");
    }

  }

  @Override
  public Optional<User> getAuthenticatedUser() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

    if (authentication == null
        || !authentication.isAuthenticated()
        || authentication.getPrincipal().equals("anonymousUser")) {
      return Optional.empty();
    }

    return Optional.of((User) authentication.getPrincipal());
  }

  @Override
  public User getRequiredAuthenticatedUser() {
    try {
      User user = getAuthenticatedUser().get();
      if (user.isBanActive())
        throw new UnauthorizedException("Account suspended");
      return user;
    } catch (ForbiddenException ex) {
      throw ex;
    } catch (Exception ex) {
      throw new UnauthorizedException("User is not authenticated");
    }
  }

}
