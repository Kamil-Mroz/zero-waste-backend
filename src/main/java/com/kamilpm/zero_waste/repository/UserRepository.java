package com.kamilpm.zero_waste.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.kamilpm.zero_waste.domain.entity.User;
import com.kamilpm.zero_waste.domain.entity.UserRole;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

  @EntityGraph(attributePaths = { "roles" })
  User findByEmail(String email);

  boolean existsByEmail(String email);

  boolean existsByEmailAndIdNot(String email, UUID id);

  @EntityGraph(attributePaths = { "roles" })
  @Query("""
      SELECT DISTINCT u
      FROM User u
      LEFT JOIN u.roles r
      WHERE u.id !=:id
      AND (:roles IS NULL OR r In :roles)
      AND (:text IS NULL
        OR LOWER(u.firstName) LIKE :text ESCAPE '\\'
        OR LOWER(u.lastName) LIKE :text ESCAPE '\\'
        OR LOWER(u.email) LIKE :text ESCAPE '\\'
      )
        """)
  Page<User> findAllByIdNot(@Param("id") UUID id, @Param("text") String text, @Param("roles") List<UserRole> roles,
      Pageable pageable);
}
