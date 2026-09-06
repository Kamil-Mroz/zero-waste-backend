package com.kamilpm.zero_waste.user.repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.kamilpm.zero_waste.user.api.UserRole;
import com.kamilpm.zero_waste.user.entity.User;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

  Optional<User> findByEmail(String email);

  boolean existsByEmail(String email);

  @Modifying
  @Query("update User u set u.banActive = false, u.bannedUntil = null where u.id IN :ids")
  void revokeBan(@Param("ids") List<UUID> ids);

  @Modifying
  @Query("update User u set u.password = :passwordHash where u.id = :id")
  void updatePassword(@Param("id") UUID id, @Param("passwordHash") String passwordHash);

  boolean existsByEmailAndIdNot(String email, UUID id);

  boolean existsByIdAndIdNot(UUID subjectId, UUID userId);

  @Query("""
          SELECT u.id
          FROM User u
          WHERE u.banActive = true
            OR u.role = :role
      """)
  Set<UUID> findIdsByBanActiveTrueOrRole(@Param("role") UserRole role);

  @Query("""
          SELECT u.id
          FROM User u
          WHERE (u.banActive = true AND u.role = 'WRITER')
            OR u.role = 'DEMO'
      """)
  Set<UUID> findIdsByBanActiveTrueAndRoleWriterOrRoleDemo();

  @Query("""
      SELECT DISTINCT u
      FROM User u
      WHERE u.id != :id
      AND (:roles IS NULL OR u.role In :roles)
      AND (:text IS NULL
        OR LOWER(u.nickname) LIKE :text ESCAPE '\\'
        OR LOWER(u.email) LIKE :text ESCAPE '\\'
      )
        """)
  Page<User> findAllByIdNot(@Param("id") UUID id, @Param("text") String text, @Param("roles") List<UserRole> roles,
      Pageable pageable);
}
