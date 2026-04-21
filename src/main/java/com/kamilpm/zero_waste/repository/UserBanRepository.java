package com.kamilpm.zero_waste.repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.kamilpm.zero_waste.domain.entity.UserBan;

public interface UserBanRepository extends JpaRepository<UserBan, UUID> {

  Optional<UserBan> findByExpiresAt(Instant expiresAt);

  Optional<UserBan> findTopByUserIdAndRevokedAtIsNullOrderByCreatedAtDesc(UUID id);

  @Query("Select b, u FROM UserBan b JOIN FETCH b.user u WHERE u.id in ?1 and b.revokedAt is null")
  List<UserBan> findBanWithUser(List<UUID> ids);
}
