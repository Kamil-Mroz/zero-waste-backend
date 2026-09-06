package com.kamilpm.zero_waste.auth.dto;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.kamilpm.zero_waste.user.api.UserAuthenticationData;
import com.kamilpm.zero_waste.user.api.UserRole;

import lombok.Getter;

@Getter
public class SecurityUser implements UserDetails {
  private final UUID id;
  private final String email;
  private final String nickname;
  private final String password;
  private final UserRole role;
  private final boolean banActive;
  private final Instant bannedUntil;
  private final Instant joinedAt;

  public SecurityUser(UserAuthenticationData user) {
    this.id = user.id();
    this.email = user.email();
    this.nickname = user.nickname();
    this.password = user.password();
    this.role = user.role();
    this.banActive = user.banActive();
    this.bannedUntil = user.bannedUntil();
    this.joinedAt = user.joinedAt();

  }

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
  }

  @Override
  public String getUsername() {
    return email;
  }

  @Override
  public String getPassword() {
    return password;
  }

  @Override
  public boolean isAccountNonExpired() {
    return true;
  }

  @Override
  public boolean isAccountNonLocked() {
    return !banActive;
  }

  @Override
  public boolean isCredentialsNonExpired() {
    return true;
  }

  @Override
  public boolean isEnabled() {
    return true;
  }

}
