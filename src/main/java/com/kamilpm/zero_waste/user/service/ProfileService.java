package com.kamilpm.zero_waste.user.service;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kamilpm.zero_waste.auth.api.CurrentUserApi;
import com.kamilpm.zero_waste.common.utils.OwnMapper;
import com.kamilpm.zero_waste.user.dto.AuthenticatedUser;
import com.kamilpm.zero_waste.user.dto.OwnProfileResponse;
import com.kamilpm.zero_waste.user.dto.ProfileQueryData;
import com.kamilpm.zero_waste.user.dto.PublicUserProfileResponse;
import com.kamilpm.zero_waste.user.dto.UserDto;
import com.kamilpm.zero_waste.user.dto.UserRole;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProfileService {

  private final UserService userService;
  private final CurrentUserApi currentUser;
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
    AuthenticatedUser user = getRequiredAuthenticatedUser();

    ProfileQueryData data = profileQueryService.getPublicProfileData(user.id());

    return OwnProfileResponse.builder()
        .items(data.items())
        .reviews(data.reviews())
        .build();
  }

  private AuthenticatedUser getRequiredAuthenticatedUser() {
    return OwnMapper.map(currentUser.getRequiredAuthenticatedUser(), (user) -> new AuthenticatedUser(
        user.id(),
        user.email(),
        user.nickname(),
        user.password(),
        UserRole.valueOf(user.role().name()),
        user.banActive(),
        user.bannedUntil(),
        user.joinedAt()));
  }

}
