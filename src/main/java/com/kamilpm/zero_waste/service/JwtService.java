package com.kamilpm.zero_waste.service;

import java.util.Map;

import com.kamilpm.zero_waste.domain.entity.User;

public interface JwtService {

  String generateToken(User user);

  String generateToken(Map<String, Object> extraClaims, User user);

  String getEmailFromToken(String token);

  boolean isTokenValid(String token);
}
