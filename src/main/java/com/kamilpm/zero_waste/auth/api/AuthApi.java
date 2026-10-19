package com.kamilpm.zero_waste.auth.api;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import com.kamilpm.zero_waste.auth.service.MyUserDetailsService;

@Service
public class AuthApi {

  private final MyUserDetailsService myUserDetailsService;

  public AuthApi(MyUserDetailsService myUserDetailsService) {
    this.myUserDetailsService = myUserDetailsService;
  }

  public UserDetails loadUserByUsername(String nickname) {
    return myUserDetailsService.loadUserByUsername(nickname);
  }

}
