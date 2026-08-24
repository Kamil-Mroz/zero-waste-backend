package com.kamilpm.zero_waste.domain.entity;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import jakarta.annotation.Nullable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Builder
@Table(name = "users")
public class User implements UserDetails {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(name = "nickname", nullable = false)
  private String nickname;

  @Column(name = "email", nullable = false, unique = true)
  @Email
  private String email;

  @Column(name = "password")
  private String password;

  @Column(name = "ban_active", nullable = false)
  private boolean banActive;

  @Column(name = "banned_until")
  private Instant bannedUntil;

  @Enumerated(EnumType.STRING)
  @Column(name = "role", nullable = false)
  private UserRole role;

  @CreationTimestamp
  @Column(name = "joined_at", nullable = false, updatable = false)
  private Instant joinedAt;

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    return authorities;
  }

  @Override
  public String getUsername() {
    return email;
  }

  @Override
  public @Nullable String getPassword() {
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
