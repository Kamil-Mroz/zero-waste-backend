package com.kamilpm.zero_waste.service.impl;

import java.time.Instant;
import java.util.Optional;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kamilpm.zero_waste.domain.entity.User;
import com.kamilpm.zero_waste.repository.UserBanRepository;
import com.kamilpm.zero_waste.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MyUserDetailsService implements UserDetailsService {
  private final UserRepository userRepository;
  private final UserBanRepository userBanRepository;

  @Override
  @Transactional(readOnly = true)
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

    Optional<User> user = userRepository.findByEmail(username);
    if (user.isPresent()) {

      User currentUser = user.get();
      clearExpiredBan(currentUser);

      return currentUser;
    }
    throw new UsernameNotFoundException("User not found with email: " + username);

  }

  private void clearExpiredBan(User user) {
    if (!user.isBanActive()) {
      return;
    }
    if (user.getBannedUntil() == null) {
      return;
    }
    if (user.getBannedUntil().isAfter(Instant.now()))
      return;

    userBanRepository.findTopByUserIdAndRevokedAtIsNullOrderByCreatedAtDesc(user.getId()).ifPresent(ban -> {
      ban.setRevokedAt(Instant.now());
      ban.setRevokedReason("Expired");
    });

    user.setBanActive(false);
    user.setBannedUntil(null);
  }

}
