package com.kamilpm.zero_waste.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.kamilpm.zero_waste.domain.entity.OAuthAccount;
import com.kamilpm.zero_waste.domain.entity.OAuthProvider;

@Repository
public interface OAuthAccountRepository extends JpaRepository<OAuthAccount, UUID> {
  @EntityGraph(attributePaths = { "user", "user.roles" })
  Optional<OAuthAccount> findByProviderAndProviderId(OAuthProvider provider, String providerId);

  @EntityGraph(attributePaths = { "user" })
  List<OAuthAccount> findByUser_Id(UUID id);

  @EntityGraph(attributePaths = { "user" })
  Optional<OAuthAccount> findByUser_IdAndProvider(UUID id, OAuthProvider provider);

  @EntityGraph(attributePaths = { "user" })
  void deleteByUser_IdIn(List<UUID> ids);

  @EntityGraph(attributePaths = { "user" })
  boolean existsByUser_IdAndProvider(UUID userId, OAuthProvider provider);
}
