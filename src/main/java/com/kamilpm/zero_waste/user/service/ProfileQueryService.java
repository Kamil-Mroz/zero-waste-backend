package com.kamilpm.zero_waste.user.service;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kamilpm.zero_waste.common.utils.OwnMapper;
import com.kamilpm.zero_waste.item.api.ItemProfileApi;
import com.kamilpm.zero_waste.review.api.ReviewProfileApi;
import com.kamilpm.zero_waste.user.dto.ItemCondition;
import com.kamilpm.zero_waste.user.dto.ItemCountBreakDown;
import com.kamilpm.zero_waste.user.dto.ItemDto;
import com.kamilpm.zero_waste.user.dto.ItemState;
import com.kamilpm.zero_waste.user.dto.ProfileItemSummary;
import com.kamilpm.zero_waste.user.dto.ProfileQueryData;
import com.kamilpm.zero_waste.user.dto.ProfileReviewSummary;
import com.kamilpm.zero_waste.user.dto.RatingBreakdown;
import com.kamilpm.zero_waste.user.dto.ReviewResponse;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProfileQueryService {

  private final ItemProfileApi itemProfileApi;
  private final ReviewProfileApi reviewProfileApi;

  public ProfileQueryData getPublicProfileData(UUID userId) {
    ProfileItemSummary items = buildItemSummary(userId);
    ProfileReviewSummary reviews = buildReviewSummary(userId);
    return new ProfileQueryData(items, reviews);

  }

  private ProfileReviewSummary buildReviewSummary(UUID id) {

    return OwnMapper.map(reviewProfileApi.buildReviewSummary(id), (reviewSummary) -> new ProfileReviewSummary(
        reviewSummary.averageRating(),
        reviewSummary.reviewCount(),
        new RatingBreakdown(
            reviewSummary.ratingBreakdown().oneStar(),
            reviewSummary.ratingBreakdown().twoStar(),
            reviewSummary.ratingBreakdown().threeStar(),
            reviewSummary.ratingBreakdown().fourStar(),
            reviewSummary.ratingBreakdown().fiveStar()),
        OwnMapper.mapList(reviewSummary.latestReviews(),
            (review) -> new ReviewResponse(review.id(), review.rating(), review.comment(), review.reviewerId(),
                review.revieweeId(), review.reviewerName(), review.moderationStatus(), review.createdAt()))));
  }

  private ProfileItemSummary buildItemSummary(UUID id) {

    return OwnMapper.map(itemProfileApi.buildItemSummary(id),
        (itemSummary) -> new ProfileItemSummary(new ItemCountBreakDown(itemSummary.itemCountBreakDown().totalItems(),
            itemSummary.itemCountBreakDown().given(), itemSummary.itemCountBreakDown().pending(),
            itemSummary.itemCountBreakDown().available()),

            OwnMapper.mapList(itemSummary.latestItems(),
                item -> new ItemDto(item.id(), item.title(), item.description(), item.city(),
                    ItemCondition.valueOf(item.condition().name()), ItemState.valueOf(item.state().name()),
                    item.moderationStatus(), null, null, null, null))));
  }

}
