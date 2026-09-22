package com.kamilpm.zero_waste.auth.api;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import com.kamilpm.zero_waste.auth.service.MyUserDetailsService;


import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthApi {

  private final MyUserDetailsService myUserDetailsService;

  public UserDetails loadUserByUsername(String nickname) {
    return myUserDetailsService.loadUserByUsername(nickname);
  }



}
