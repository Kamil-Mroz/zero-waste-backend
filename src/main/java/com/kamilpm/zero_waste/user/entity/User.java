package com.kamilpm.zero_waste.user.entity;

import java.time.Instant;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;

import com.kamilpm.zero_waste.common.dto.UserRole;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;

@Entity
@Table(name = "users")
public class User {
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

  public User() {
  }

  public User(UUID id, String nickname, @Email String email, String password, boolean banActive, Instant bannedUntil,
      UserRole role, Instant joinedAt) {
    this.id = id;
    this.nickname = nickname;
    this.email = email;
    this.password = password;
    this.banActive = banActive;
    this.bannedUntil = bannedUntil;
    this.role = role;
    this.joinedAt = joinedAt;
  }

  public UUID getId() {
    return id;
  }

  public void setId(UUID id) {
    this.id = id;
  }

  public String getNickname() {
    return nickname;
  }

  public void setNickname(String nickname) {
    this.nickname = nickname;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public boolean isBanActive() {
    return banActive;
  }

  public void setBanActive(boolean banActive) {
    this.banActive = banActive;
  }

  public Instant getBannedUntil() {
    return bannedUntil;
  }

  public void setBannedUntil(Instant bannedUntil) {
    this.bannedUntil = bannedUntil;
  }

  public UserRole getRole() {
    return role;
  }

  public void setRole(UserRole role) {
    this.role = role;
  }

  public Instant getJoinedAt() {
    return joinedAt;
  }

  public void setJoinedAt(Instant joinedAt) {
    this.joinedAt = joinedAt;
  }

  @Override
  public String toString() {
    return "User [id=" + id + ", nickname=" + nickname + ", email=" + email + ", password=" + password + ", banActive="
        + banActive + ", bannedUntil=" + bannedUntil + ", role=" + role + ", joinedAt=" + joinedAt + "]";
  }

}
