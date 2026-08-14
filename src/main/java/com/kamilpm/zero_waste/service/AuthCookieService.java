package com.kamilpm.zero_waste.service;

import jakarta.servlet.http.HttpServletResponse;

public interface AuthCookieService {
  void addRefreshCookie(HttpServletResponse response, String refreshToken);

  void clearRefreshCookie(HttpServletResponse response);

}
