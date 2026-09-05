package com.kamilpm.zero_waste.user.repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.kamilpm.zero_waste.user.entity.UserBan;

public interface UserBanRepository extends JpaRepository<UserBan, UUID> {

  Optional<UserBan> findByExpiresAt(Instant expiresAt);

  Optional<UserBan> findTopByUserIdAndRevokedAtIsNullOrderByCreatedAtDesc(UUID id);

  @Query("Select b FROM UserBan b WHERE b.userId in ?1 and b.revokedAt is null")
  List<UserBan> findBanWithUser(List<UUID> ids);

  @Modifying
  @Query("delete from UserBan b where b.userId in :userIds")
  void deleteAllByUserIds(@Param("userIds") List<UUID> userId);
}
