package com.kamilpm.zero_waste.service;

import java.util.Optional;

import org.springframework.security.core.Authentication;

import com.kamilpm.zero_waste.domain.entity.User;
import com.kamilpm.zero_waste.domain.request.LoginRequest;

public interface AuthService {
  User register(User user);

  Authentication verify(LoginRequest loginRequest);

  Optional<User> getAuthenticatedUser();

  User getRequiredAuthenticatedUser();
}
