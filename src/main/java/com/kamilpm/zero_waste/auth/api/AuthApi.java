package com.kamilpm.zero_waste.auth.api;

import java.util.Optional;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import com.kamilpm.zero_waste.auth.dto.SecurityUser;
import com.kamilpm.zero_waste.auth.service.MyUserDetailsService;
import com.kamilpm.zero_waste.common.exception.UnauthorizedException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthApi {

  private final MyUserDetailsService myUserDetailsService;

  public UserDetails loadUserByUsername(String nickname) {
    return myUserDetailsService.loadUserByUsername(nickname);

  }

  public Optional<AuthenticatedUser> getAuthenticatedUser() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

    if (authentication == null || !authentication.isAuthenticated()
        || "anonymousUser".equals(authentication.getPrincipal())) {
      return Optional.empty();
    }
    Object principal = authentication.getPrincipal();
    if (!(principal instanceof SecurityUser securityUser)) {
      return Optional.empty();
    }
    return Optional.of(toAuthenticatedUser(securityUser));

  }

  public AuthenticatedUser getRequiredAuthenticatedUser() {
    return getAuthenticatedUser().orElseThrow(() -> new UnauthorizedException("You are not authenticated"));
  }

  private AuthenticatedUser toAuthenticatedUser(SecurityUser user) {
    return new AuthenticatedUser(user.getId(), user.getEmail(), user.getNickname(), user.getPassword(), user.getRole(),
        user.isBanActive(), user.getBannedUntil(), user.getJoinedAt());

  }

}
