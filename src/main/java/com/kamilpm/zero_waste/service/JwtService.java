package com.kamilpm.zero_waste.service;

import java.util.Map;

import org.springframework.security.core.Authentication;

public interface JwtService {

  String generateToken(Authentication authentication);

  String generateToken(Map<String, Object> extraClaims, Authentication authentication);

  String getEmailFromToken(String token);

  boolean isTokenValid(String token);
}
