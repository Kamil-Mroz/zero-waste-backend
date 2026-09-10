package com.kamilpm.zero_waste.security;

import java.util.Objects;
import java.util.function.Supplier;

import org.jspecify.annotations.Nullable;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.authorization.AuthorizationResult;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;
import org.springframework.stereotype.Component;

@Component
public class DemoReadOnlyAuthorizationManage implements AuthorizationManager<RequestAuthorizationContext> {
  @Override
  public @Nullable AuthorizationResult authorize(Supplier<? extends @Nullable Authentication> authentication,
      RequestAuthorizationContext context) {
    boolean isDemo = authentication.get().getAuthorities().stream()
        .anyMatch(authority -> Objects.equals(authority.getAuthority(), "ROLE_DEMO"));
    return new AuthorizationDecision(!isDemo);
  }

}
