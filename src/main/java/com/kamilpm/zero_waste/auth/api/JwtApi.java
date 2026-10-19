package com.kamilpm.zero_waste.auth.api;

import org.springframework.stereotype.Service;

import com.kamilpm.zero_waste.auth.service.JwtService;

@Service
public class JwtApi {

  private final JwtService jwtService;

  public JwtApi(JwtService jwtService) {
    this.jwtService = jwtService;
  }

  public boolean isTokenValid(String token) {

    return jwtService.isTokenValid(token);
  }

  public String getEmailFromToken(String token) {
    return jwtService.getEmailFromToken(token);
  }

}
