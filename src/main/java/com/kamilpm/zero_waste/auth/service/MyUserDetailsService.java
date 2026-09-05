package com.kamilpm.zero_waste.auth.service;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kamilpm.zero_waste.auth.dto.SecurityUser;
import com.kamilpm.zero_waste.user.api.UserApi;
import com.kamilpm.zero_waste.user.api.UserAuthenticationData;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MyUserDetailsService implements UserDetailsService {
  private final UserApi userApi;

  @Override
  @Transactional(readOnly = true)
  public UserDetails loadUserByUsername(String nickname) {

    UserAuthenticationData user = userApi.findAuthenticationData(nickname);

    return new SecurityUser(user);

  }

}
