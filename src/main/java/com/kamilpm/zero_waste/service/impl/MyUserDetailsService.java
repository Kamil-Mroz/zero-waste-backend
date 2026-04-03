package com.kamilpm.zero_waste.service.impl;

import java.util.Optional;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.kamilpm.zero_waste.domain.MyUserDetails;
import com.kamilpm.zero_waste.domain.entity.User;
import com.kamilpm.zero_waste.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MyUserDetailsService implements UserDetailsService {
  private final UserRepository userRepository;

  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

    User user = Optional.ofNullable(userRepository.findByEmail(username))
        .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + username));
    return new MyUserDetails(user);
  }

}
