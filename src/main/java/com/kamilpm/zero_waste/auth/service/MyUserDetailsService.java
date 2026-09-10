package com.kamilpm.zero_waste.auth.service;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kamilpm.zero_waste.auth.dto.AuthenticatedUser;
import com.kamilpm.zero_waste.auth.dto.SecurityUser;
import com.kamilpm.zero_waste.auth.dto.UserRole;
import com.kamilpm.zero_waste.common.utils.OwnMapper;
import com.kamilpm.zero_waste.user.api.UserApi;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MyUserDetailsService implements UserDetailsService {
  private final UserApi userApi;

  @Override
  @Transactional(readOnly = true)
  public UserDetails loadUserByUsername(String nickname) {

    AuthenticatedUser user = findAuthenticatedUser(nickname);

    return new SecurityUser(user);

  }

  private AuthenticatedUser findAuthenticatedUser(String nickname) {
    return OwnMapper.map(userApi.findAuthenticationData(nickname),
        (user) -> new AuthenticatedUser(user.id(), user.email(), user.nickname(), user.password(),
            UserRole.valueOf(user.role().name()),
            user.banActive(), user.bannedUntil(), user.joinedAt()));

  }

}
