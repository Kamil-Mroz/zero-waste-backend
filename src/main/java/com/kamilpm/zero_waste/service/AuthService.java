package com.kamilpm.zero_waste.service;

import java.util.Optional;

import org.springframework.security.core.Authentication;

import com.kamilpm.zero_waste.domain.entity.User;
import com.kamilpm.zero_waste.domain.request.LoginRequest;
import com.kamilpm.zero_waste.domain.request.RegisterRequest;
import com.kamilpm.zero_waste.security.MyUserDetails;

public interface AuthService {
  User register(RegisterRequest registerRequest);

  Authentication verify(LoginRequest loginRequest);

  Optional<MyUserDetails> getAuthenticatedUser();

  MyUserDetails getRequiredAuthenticatedUserDetails();

  User getRequiredAuthenticatedUserEntity();
}
