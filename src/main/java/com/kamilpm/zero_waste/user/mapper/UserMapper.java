package com.kamilpm.zero_waste.user.mapper;

import org.springframework.stereotype.Component;

import com.kamilpm.zero_waste.common.dto.CurrentUser;
import com.kamilpm.zero_waste.common.dto.UserAuthenticationData;
import com.kamilpm.zero_waste.common.dto.UserSummaryDto;
import com.kamilpm.zero_waste.common.dto.UserSummaryWithEmailDto;
import com.kamilpm.zero_waste.user.dto.UserDto;
import com.kamilpm.zero_waste.user.entity.User;

@Component
public class UserMapper {

  public UserDto toDto(User user) {

    return new UserDto(user.getId(), user.getNickname(), user.getEmail(), user.isBanActive(), user.getBannedUntil(),
        user.getJoinedAt(), user.getRole());
  }

  public CurrentUser toAuthenticatedUser(User user) {
    return new CurrentUser(user.getId(), user.getEmail(), user.getNickname(), user.getPassword(), user.getRole(),
        user.isBanActive(), user.getBannedUntil(), user.getJoinedAt());
  }

  public UserSummaryDto toUserSummaryDto(User user) {
    return new UserSummaryDto(user.getId(), user.getNickname());
  }

  public UserSummaryWithEmailDto toUserSummaryWithEmailDto(User user) {
    return new UserSummaryWithEmailDto(user.getId(), user.getNickname(), user.getEmail());
  }

  public UserAuthenticationData toUserAuthenticationData(User user) {
    return new UserAuthenticationData(user.getId(), user.getEmail(), user.getNickname(), user.getPassword(),
        user.getRole(),
        user.isBanActive(), user.getBannedUntil(), user.getJoinedAt());
  }

}
