package com.kamilpm.zero_waste.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.kamilpm.zero_waste.domain.entity.RefreshToken;
import com.kamilpm.zero_waste.domain.entity.User;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

  @EntityGraph(attributePaths = { "user", "user.roles" })
  Optional<RefreshToken> findByToken(String token);

  @EntityGraph(attributePaths = { "user" })
  List<RefreshToken> findAllByUser(User user);

  @Modifying
  @EntityGraph(attributePaths = { "user" })
  @Query("""
      update RefreshToken rt
      set rt.revoked = true
      where rt.user.id = :id
        and rt.revoked = false

        """)
  void revokeAllByUserId(@Param(value = "id") UUID id);

  @Modifying
  @EntityGraph(attributePaths = { "user" })
  @Query("delete from RefreshToken r where r.user.id in :userIds")
  void deleteAllByUserIds(@Param(value = "userIds") List<UUID> userId);
}
