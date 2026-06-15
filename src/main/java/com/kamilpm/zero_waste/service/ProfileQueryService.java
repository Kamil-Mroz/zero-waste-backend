package com.kamilpm.zero_waste.service;

import java.util.UUID;

import com.kamilpm.zero_waste.domain.dto.ProfileQueryData;

public interface ProfileQueryService {
  ProfileQueryData getPublicProfileData(UUID userId);

}
