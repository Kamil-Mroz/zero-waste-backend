package com.kamilpm.zero_waste.common.security;

import java.security.Principal;
import java.util.Objects;

import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import com.kamilpm.zero_waste.auth.api.AuthApi;
import com.kamilpm.zero_waste.auth.api.JwtApi;
import com.kamilpm.zero_waste.common.exception.ForbiddenException;
import com.kamilpm.zero_waste.common.exception.TokenException;
import com.kamilpm.zero_waste.common.exception.UnauthorizedException;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class WebSocketAuthInterceptor implements ChannelInterceptor {

  private final JwtApi jwtApi;
  private final AuthApi authApi;

  @Override
  public Message<?> preSend(Message<?> message, MessageChannel channel) {
    StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
    if (accessor == null || accessor.getCommand() == null) {
      return message;
    }
    switch (accessor.getCommand()) {

      case CONNECT -> authenticate(accessor);

      case SUBSCRIBE -> authorizeSubscription(accessor);

      default -> {
      }
    }
    return message;

  }

  private void authenticate(StompHeaderAccessor accessor) {

    String authHeader = accessor.getFirstNativeHeader("Authorization");
    if (authHeader == null || !authHeader.startsWith("Bearer ")) {
      throw new TokenException("Missing token");
    }

    String token = authHeader.substring(7);
    if (!jwtApi.isTokenValid(token)) {
      throw new TokenException("Invalid token");

    }

    String email = jwtApi.getEmailFromToken(token);

    UserDetails userDetails = authApi.loadUserByUsername(email);
    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userDetails, null,
        userDetails.getAuthorities());
    accessor.setUser(authentication);

  }

  private void authorizeSubscription(StompHeaderAccessor accessor) {
    String destination = accessor.getDestination();

    if (!Objects.equals("/topic/reports", destination)) {
      return;
    }

    Principal principal = accessor.getUser();
    if (principal == null) {
      throw new UnauthorizedException("Not authenticated");
    }

    if (!(principal instanceof Authentication authentication)) {
      throw new UnauthorizedException("Invalid authentication");
    }

    boolean isAdmin = authentication.getAuthorities().stream()
        .anyMatch(authority -> Objects.equals(authority.getAuthority(), "ROLE_ADMIN"));
    if (!isAdmin) {
      throw new ForbiddenException("Only administrators can subscribe to reports");
    }
  }

}
