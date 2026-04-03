package com.kamilpm.zero_waste.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.kamilpm.zero_waste.domain.entity.RefreshToken;
import com.kamilpm.zero_waste.domain.entity.User;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
  Optional<RefreshToken> findByToken(String token);

  List<RefreshToken> findAllByUser(User user);
}
