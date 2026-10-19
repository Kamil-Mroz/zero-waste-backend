package com.kamilpm.zero_waste.auth.service;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kamilpm.zero_waste.auth.dto.SecurityUser;
import com.kamilpm.zero_waste.common.dto.UserAuthenticationData;
import com.kamilpm.zero_waste.common.interfaces.UserProvider;

@Service
public class MyUserDetailsService implements UserDetailsService {
  private final UserProvider userApi;

  public MyUserDetailsService(UserProvider userProvider) {
    this.userApi = userProvider;
  }

  @Override
  @Transactional(readOnly = true)
  public UserDetails loadUserByUsername(String nickname) {

    UserAuthenticationData user = userApi.findAuthenticationData(nickname);

    return new SecurityUser(user);

  }

}
