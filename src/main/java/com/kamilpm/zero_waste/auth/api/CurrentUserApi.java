package com.kamilpm.zero_waste.auth.api;

import java.util.Optional;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import com.kamilpm.zero_waste.auth.dto.SecurityUser;
import com.kamilpm.zero_waste.common.dto.CurrentUser;
import com.kamilpm.zero_waste.common.exception.UnauthorizedException;
import com.kamilpm.zero_waste.common.interfaces.CurrentUserProvider;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class CurrentUserApi implements CurrentUserProvider {
  public Optional<CurrentUser> getAuthenticatedUser() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

    if (authentication == null || !authentication.isAuthenticated()
        || "anonymousUser".equals(authentication.getPrincipal())) {
      return Optional.empty();
    }
    Object principal = authentication.getPrincipal();
    if (!(principal instanceof SecurityUser securityUser)) {
      return Optional.empty();
    }
    return Optional.of(toCurrentUser(securityUser));
  }

  public CurrentUser getRequiredAuthenticatedUser() {
    return getAuthenticatedUser().orElseThrow(() -> new UnauthorizedException("You are not authenticated"));
  }

  private CurrentUser toCurrentUser(SecurityUser user) {
    return new CurrentUser(user.getId(), user.getEmail(),
        user.getNickname(), user.getPassword(), user.getRole(),
        user.isBanActive(), user.getBannedUntil(), user.getJoinedAt());

  }

}
