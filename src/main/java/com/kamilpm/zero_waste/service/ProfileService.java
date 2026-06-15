package com.kamilpm.zero_waste.service;

import java.util.UUID;

import com.kamilpm.zero_waste.domain.response.OwnProfileResponse;
import com.kamilpm.zero_waste.domain.response.PublicUserProfileResponse;

public interface ProfileService {
  PublicUserProfileResponse getProfile(UUID userId);

  OwnProfileResponse getOwnProfile();

}
