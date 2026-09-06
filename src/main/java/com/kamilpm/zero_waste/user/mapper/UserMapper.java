package com.kamilpm.zero_waste.user.mapper;

import org.springframework.stereotype.Component;

import com.kamilpm.zero_waste.auth.api.AuthenticatedUser;

import com.kamilpm.zero_waste.user.api.UserDto;
import com.kamilpm.zero_waste.user.entity.User;

@Component
public class UserMapper {

  public UserDto toDto(User user) {

    return new UserDto(user.getId(), user.getNickname(), user.getEmail(), user.isBanActive(), user.getBannedUntil(),
        user.getJoinedAt(), user.getRole());
  }

  public AuthenticatedUser toAuthenticatedUser(User user) {
    return new AuthenticatedUser(user.getId(), user.getEmail(), user.getNickname(), user.getPassword(), user.getRole(),
        user.isBanActive(), user.getBannedUntil(), user.getJoinedAt());
  }

}
