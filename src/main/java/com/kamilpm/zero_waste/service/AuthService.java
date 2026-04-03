package com.kamilpm.zero_waste.service;

import org.springframework.security.core.userdetails.UserDetails;

import com.kamilpm.zero_waste.domain.dto.LoginRequest;
import com.kamilpm.zero_waste.domain.entity.User;

public interface AuthService {
  User register(User user);

  UserDetails verify(LoginRequest loginRequest);

  User getAuthenticatedUser();
}
