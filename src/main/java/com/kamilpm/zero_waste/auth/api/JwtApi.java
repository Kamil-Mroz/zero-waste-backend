package com.kamilpm.zero_waste.auth.api;

import org.springframework.stereotype.Service;

import com.kamilpm.zero_waste.auth.service.JwtService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class JwtApi {

  private final JwtService jwtService;

  public boolean isTokenValid(String token) {

    return jwtService.isTokenValid(token);
  }

  public String getEmailFromToken(String token) {
    return jwtService.getEmailFromToken(token);
  }

}
