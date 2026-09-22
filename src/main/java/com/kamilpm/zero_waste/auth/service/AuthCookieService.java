package com.kamilpm.zero_waste.auth.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class AuthCookieService {
  @Value("${app.prod}")
  private boolean isProd;

  @Value("${refresh-token.expiration}")
  private long refreshTokenExpiration;

  public void addRefreshCookie(HttpServletResponse response, String refreshToken) {
    Cookie cookie = new Cookie(
        "refreshToken",
        refreshToken);

    cookie.setHttpOnly(true);
    cookie.setSecure(isProd);
    cookie.setPath("/");

    if (isProd) {
      cookie.setAttribute(
          "SameSite",
          "Strict");
    }

    cookie.setMaxAge(
        (int) refreshTokenExpiration);

    response.addCookie(cookie);
  }

  public void clearRefreshCookie(HttpServletResponse response) {

    Cookie cookie = new Cookie("refreshToken", null);
    cookie.setHttpOnly(true);

    cookie.setSecure(isProd);
    cookie.setPath("/");
    cookie.setMaxAge(0);

    if (isProd) {
      cookie.setAttribute("SameSite", "Strict");
    }

    response.addCookie(cookie);
  }
}
