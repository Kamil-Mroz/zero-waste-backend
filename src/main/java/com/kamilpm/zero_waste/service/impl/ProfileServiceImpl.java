package com.kamilpm.zero_waste.service.impl;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kamilpm.zero_waste.domain.dto.ProfileQueryData;
import com.kamilpm.zero_waste.domain.dto.UserDto;
import com.kamilpm.zero_waste.domain.response.OwnProfileResponse;
import com.kamilpm.zero_waste.domain.response.PublicUserProfileResponse;
import com.kamilpm.zero_waste.security.MyUserDetails;
import com.kamilpm.zero_waste.service.AuthService;
import com.kamilpm.zero_waste.service.ProfileQueryService;
import com.kamilpm.zero_waste.service.ProfileService;
import com.kamilpm.zero_waste.service.UserService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProfileServiceImpl implements ProfileService {

  private final UserService userService;
  private final AuthService authService;
  private final ProfileQueryService profileQueryService;

  @Override
  @Transactional(readOnly = true)
  public PublicUserProfileResponse getProfile(UUID userId) {

    UserDto user = userService.getUser(userId);
    ProfileQueryData data = profileQueryService.getPublicProfileData(user.getId());
    return PublicUserProfileResponse.builder()
        .id(user.getId())
        .firstName(user.getFirstName())
        .lastName(user.getLastName())
        .joinedAt(user.getJoinedAt())
        .items(data.items())
        .reviews(data.reviews())
        .build();
  }

  @Override
  @Transactional(readOnly = true)
  public OwnProfileResponse getOwnProfile() {
    MyUserDetails user = authService.getRequiredAuthenticatedUserDetails();

    ProfileQueryData data = profileQueryService.getPublicProfileData(user.getId());

    return OwnProfileResponse.builder()
        .items(data.items())
        .reviews(data.reviews())
        .build();

  }

}
