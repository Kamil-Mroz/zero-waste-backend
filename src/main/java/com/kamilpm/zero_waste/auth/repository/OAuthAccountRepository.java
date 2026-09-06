package com.kamilpm.zero_waste.auth.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.kamilpm.zero_waste.auth.entity.OAuthAccount;
import com.kamilpm.zero_waste.auth.entity.OAuthProvider;

@Repository
public interface OAuthAccountRepository extends JpaRepository<OAuthAccount, UUID> {
  Optional<OAuthAccount> findByProviderAndProviderId(OAuthProvider provider, String providerId);

  List<OAuthAccount> findByUserId(UUID id);

  Optional<OAuthAccount> findByUserIdAndProvider(UUID id, OAuthProvider provider);

  void deleteByUserIdIn(List<UUID> ids);

  boolean existsByUserIdAndProvider(UUID userId, OAuthProvider provider);
}
