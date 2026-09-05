package com.kamilpm.zero_waste.user.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kamilpm.zero_waste.common.entity.ModerationStatus;
import com.kamilpm.zero_waste.common.interfaces.IRatingBreakdownWithStats;
import com.kamilpm.zero_waste.item.api.ItemCountBreakDown;
import com.kamilpm.zero_waste.item.api.ItemProfileApi;
import com.kamilpm.zero_waste.review.api.ReviewProfileApi;
import com.kamilpm.zero_waste.user.api.ProfileItemSummary;
import com.kamilpm.zero_waste.user.api.ProfileReviewSummary;
import com.kamilpm.zero_waste.user.dto.ProfileQueryData;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProfileQueryService {

  private final ItemProfileApi itemProfileApi;
  private final ReviewProfileApi reviewProfileApi;

  public ProfileQueryData getPublicProfileData(UUID userId) {
    ProfileItemSummary items = itemProfileApi.buildItemSummary(userId);
    ProfileReviewSummary reviews = reviewProfileApi.buildReviewSummary(userId);
    return new ProfileQueryData(items, reviews);

  }

}
