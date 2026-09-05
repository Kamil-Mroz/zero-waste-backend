package com.kamilpm.zero_waste.auth.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.kamilpm.zero_waste.auth.entity.RefreshToken;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

  Optional<RefreshToken> findByToken(String token);

  @Modifying
  @Query("""
      update RefreshToken rt
      set rt.revoked = true
      where rt.userId IN :ids
        and rt.revoked = false

        """)
  void revokeAllByUserIds(@Param("ids") List<UUID> ids);

  @Modifying
  @Query("delete from RefreshToken r where r.userId in :userIds")
  void deleteAllByUserIds(@Param("userIds") List<UUID> userId);
}
