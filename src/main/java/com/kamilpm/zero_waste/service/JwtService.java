package com.kamilpm.zero_waste.service;

import java.util.Map;

import org.springframework.security.core.userdetails.UserDetails;

public interface JwtService {

  String generateToken(UserDetails userDetails);

  String generateToken(Map<String, Object> extraClaims, UserDetails userDetails);

  String getEmailFromToken(String token);

  boolean isTokenValid(String token);
}
