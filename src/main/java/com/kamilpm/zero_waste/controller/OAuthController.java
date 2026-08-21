package com.kamilpm.zero_waste.controller;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.kamilpm.zero_waste.config.OAuthProperties;
import com.kamilpm.zero_waste.domain.dto.Connections;
import com.kamilpm.zero_waste.domain.dto.OAuthFlow;
import com.kamilpm.zero_waste.domain.dto.OAuthSession;
import com.kamilpm.zero_waste.domain.dto.OAuthUserInfo;
import com.kamilpm.zero_waste.domain.entity.OAuthProvider;
import com.kamilpm.zero_waste.domain.entity.User;
import com.kamilpm.zero_waste.exception.ApiException;
import com.kamilpm.zero_waste.exception.OAuthAuthenticationException;
import com.kamilpm.zero_waste.service.AuthCookieService;
import com.kamilpm.zero_waste.service.OAuthService;
import com.kamilpm.zero_waste.service.RefreshTokenService;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor

@RequestMapping(path = "/api/v{version}/oauth", version = "1")
public class OAuthController {

  private final OAuthService oauthService;
  private final OAuthProperties properties;
  private final RefreshTokenService refreshTokenService;
  private final AuthCookieService authCookieService;

  @GetMapping("/callback/{provider}")
  public void callback(
      @PathVariable OAuthProvider provider,
      @RequestParam(required = false) String code,
      @RequestParam(required = false) String state,
      @RequestParam(required = false) String error,
      HttpServletResponse response) throws IOException {
    String frontendUrl = properties.frontendUrl();
    OAuthSession session = null;
    try {
      session = oauthService.consume(state);
      if (session.provider() != provider) {
        throw new OAuthAuthenticationException("OAuth provider mismatch");
      }
      OAuthUserInfo info = oauthService.authenticate(provider, code);

      switch (session.flow()) {
        case LOGIN -> {
          User user = oauthService.processLogin(info);
          String refreshToken = refreshTokenService.generateRefreshToken(user).getToken();
          authCookieService.addRefreshCookie(response, refreshToken);
          response.sendRedirect(properties.frontendUrl() + "/profile");
        }
        case LINK -> {
          oauthService.linkOAuthAccount(session.userId(), info);
          response.sendRedirect(properties.frontendUrl() + "/settings");
        }
      }

    } catch (ApiException ex) {
      if (session != null && session.flow() == OAuthFlow.LINK) {
        redirectLinkError(response, ex.getMessage());
      } else {
        redirectLoginError(response, ex.getMessage());
      }
    }
  }

  private void redirectLinkError(
      HttpServletResponse response,
      String message)
      throws IOException {
    redirect(
        response,
        properties.frontendUrl() + "/settings",
        "error",
        message);
  }

  private void redirectLoginError(
      HttpServletResponse response,
      String message)
      throws IOException {
    redirect(
        response,
        properties.frontendUrl() + "/login",
        "error",
        message);
  }

  private void redirect(
      HttpServletResponse response,
      String url,
      String parameter,
      String value)
      throws IOException {

    response.sendRedirect(
        url +
            "?" +
            parameter +
            "=" +
            URLEncoder.encode(
                value,
                StandardCharsets.UTF_8));
  }

  @PostMapping("/login/{provider}")
  public ResponseEntity<?> login(@PathVariable OAuthProvider provider) {
    return ResponseEntity.ok(Map.of("redirectUrl",
        oauthService.initiateLogin(provider)));
  }

  @PostMapping("/link/{provider}")
  public ResponseEntity<?> initiateLink(@PathVariable OAuthProvider provider) {

    return ResponseEntity.ok(Map.of("redirectUrl", oauthService.initiateLink(provider)));
  }

  @DeleteMapping("/link/{provider}")
  public ResponseEntity<Void> unlinkAccount(@PathVariable OAuthProvider provider) {
    oauthService.unlinkAccount(provider);
    return ResponseEntity.noContent().build();
  }

  @GetMapping("/connections")
  public ResponseEntity<Connections> getConnections() {
    return ResponseEntity.ok(oauthService.getConnections());
  }
}
