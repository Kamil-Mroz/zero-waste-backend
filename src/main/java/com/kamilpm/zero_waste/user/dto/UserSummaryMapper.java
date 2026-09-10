package com.kamilpm.zero_waste.user.dto;

import org.springframework.stereotype.Component;
import com.kamilpm.zero_waste.user.entity.User;

@Component
public class UserSummaryMapper {

  public UserSummaryDto toDto(AuthenticatedUser user) {
    if (user == null) {
      return null;
    }
    return new UserSummaryDto(user.id(), user.nickname());
  };

  public UserSummaryDto toDto(User user) {
    if (user == null) {
      return null;
    }
    return new UserSummaryDto(user.getId(), user.getNickname());
  };

  public UserSummaryWithEmailDto toWithEmailDto(User user) {
    if (user == null) {
      return null;
    }
    return new UserSummaryWithEmailDto(user.getId(), user.getNickname(), user.getEmail());
  };

}
