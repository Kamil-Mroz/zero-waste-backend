package com.kamilpm.zero_waste.user.service;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kamilpm.zero_waste.common.dto.CurrentUser;
import com.kamilpm.zero_waste.common.interfaces.CurrentUserProvider;
import com.kamilpm.zero_waste.user.dto.OwnProfileResponse;
import com.kamilpm.zero_waste.user.dto.ProfileQueryData;
import com.kamilpm.zero_waste.user.dto.PublicUserProfileResponse;
import com.kamilpm.zero_waste.user.dto.UserDto;

@Service
public class ProfileService {

  private final UserService userService;
  private final CurrentUserProvider currentUser;
  private final ProfileQueryService profileQueryService;

  public ProfileService(UserService userService, CurrentUserProvider currentUser,
      ProfileQueryService profileQueryService) {
    this.userService = userService;
    this.currentUser = currentUser;
    this.profileQueryService = profileQueryService;
  }

  @Transactional(readOnly = true)
  public PublicUserProfileResponse getProfile(UUID userId) {

    UserDto user = userService.getUser(userId);
    ProfileQueryData data = profileQueryService.getPublicProfileData(user.id());
    return new PublicUserProfileResponse(user.id(),
        user.nickname(),
        user.joinedAt(),
        user.activeBan(),
        data.items(),
        data.reviews());
  }

  @Transactional(readOnly = true)
  public OwnProfileResponse getOwnProfile() {
    CurrentUser user = currentUser.getRequiredAuthenticatedUser();

    ProfileQueryData data = profileQueryService.getPublicProfileData(user.id());

    return new OwnProfileResponse(
        data.items(),
        data.reviews());
  }

}
