package com.kamilpm.zero_waste.user.service;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kamilpm.zero_waste.common.dto.ProfileItemSummary;
import com.kamilpm.zero_waste.common.dto.ProfileReviewSummary;
import com.kamilpm.zero_waste.common.interfaces.ItemProvider;
import com.kamilpm.zero_waste.common.interfaces.ReviewProvider;
import com.kamilpm.zero_waste.user.dto.ProfileQueryData;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProfileQueryService {

  private final ItemProvider itemProfileApi;
  private final ReviewProvider reviewProfileApi;

  public ProfileQueryData getPublicProfileData(UUID userId) {
    ProfileItemSummary items = itemProfileApi.buildItemSummary(userId);
    ProfileReviewSummary reviews = reviewProfileApi.buildReviewSummary(userId);
    return new ProfileQueryData(items, reviews);

  }

}
