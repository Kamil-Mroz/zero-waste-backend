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

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProfileService {

  private final UserService userService;
  private final CurrentUserProvider currentUser;
  private final ProfileQueryService profileQueryService;

  @Transactional(readOnly = true)
  public PublicUserProfileResponse getProfile(UUID userId) {

    UserDto user = userService.getUser(userId);
    ProfileQueryData data = profileQueryService.getPublicProfileData(user.getId());
    return PublicUserProfileResponse.builder()
        .id(user.getId())
        .banned(user.isActiveBan())
        .nickname(user.getNickname())
        .joinedAt(user.getJoinedAt())
        .items(data.items())
        .reviews(data.reviews())
        .build();
  }

  @Transactional(readOnly = true)
  public OwnProfileResponse getOwnProfile() {
    CurrentUser user = currentUser.getRequiredAuthenticatedUser();

    ProfileQueryData data = profileQueryService.getPublicProfileData(user.id());

    return OwnProfileResponse.builder()
        .items(data.items())
        .reviews(data.reviews())
        .build();
  }

}
