package com.kamilpm.zero_waste.auth.service;

import com.kamilpm.zero_waste.user.api.UserApi;
import java.util.Objects;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.kamilpm.zero_waste.auth.api.AuthApi;
import com.kamilpm.zero_waste.auth.api.AuthenticatedUser;
import com.kamilpm.zero_waste.auth.dto.CreatePasswordRequest;
import com.kamilpm.zero_waste.auth.dto.LoginRequest;
import com.kamilpm.zero_waste.auth.dto.UpdatePasswordRequest;
import com.kamilpm.zero_waste.common.exception.BadCredentialsExceptionCustom;
import com.kamilpm.zero_waste.common.exception.ConflictException;
import com.kamilpm.zero_waste.common.exception.ForbiddenException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {
  private final UserApi userApi;
  private final PasswordEncoder passwordEncoder;
  private final AuthenticationManager authenticationManager;
  private final AuthApi authApi;

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
    AuthenticatedUser user = authApi.getRequiredAuthenticatedUser();
    if (user.password() != null) {
      throw new ConflictException("Password already set");
    }
    if (!Objects.equals(passwords.newPassword(), passwords.confirmPassword())) {
      throw new ConflictException("Passwords do not match");
    }

    userApi.savePassword(user.id(), passwordEncoder.encode(passwords.newPassword()));
  }

  public void handlePasswordUpdate(UpdatePasswordRequest passwords) {
    AuthenticatedUser user = authApi.getRequiredAuthenticatedUser();
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

}
