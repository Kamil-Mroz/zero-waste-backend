package com.kamilpm.zero_waste.auth.entity;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "oauth_accounts", uniqueConstraints = {
    @UniqueConstraint(name = "uk_oauth_provider_user", columnNames = { "provider", "provider_id" })
})
public class OAuthAccount {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(name = "user_id", nullable = false)
  private UUID userId;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private OAuthProvider provider;

  @Column(name = "provider_id", nullable = false)
  private String providerId;

  public OAuthAccount() {
  }

  public OAuthAccount(UUID id, UUID userId, OAuthProvider provider, String providerId) {
    this.id = id;
    this.userId = userId;
    this.provider = provider;
    this.providerId = providerId;
  }

  public UUID getId() {
    return id;
  }

  public void setId(UUID id) {
    this.id = id;
  }

  public UUID getUserId() {
    return userId;
  }

  public void setUserId(UUID userId) {
    this.userId = userId;
  }

  public OAuthProvider getProvider() {
    return provider;
  }

  public void setProvider(OAuthProvider provider) {
    this.provider = provider;
  }

  public String getProviderId() {
    return providerId;
  }

  public void setProviderId(String providerId) {
    this.providerId = providerId;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((id == null) ? 0 : id.hashCode());
    result = prime * result + ((userId == null) ? 0 : userId.hashCode());
    result = prime * result + ((provider == null) ? 0 : provider.hashCode());
    result = prime * result + ((providerId == null) ? 0 : providerId.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    OAuthAccount other = (OAuthAccount) obj;
    if (id == null) {
      if (other.id != null)
        return false;
    } else if (!id.equals(other.id))
      return false;
    if (userId == null) {
      if (other.userId != null)
        return false;
    } else if (!userId.equals(other.userId))
      return false;
    if (provider != other.provider)
      return false;
    if (providerId == null) {
      if (other.providerId != null)
        return false;
    } else if (!providerId.equals(other.providerId))
      return false;
    return true;
  }

}
