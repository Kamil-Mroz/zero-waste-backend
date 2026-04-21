package com.kamilpm.zero_waste.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.kamilpm.zero_waste.domain.entity.User;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

  User findByEmail(String email);

  boolean existsByEmail(String email);

  boolean existsByEmailAndIdNot(String email, UUID id);

  List<User> findByIdNot(UUID id);
}
