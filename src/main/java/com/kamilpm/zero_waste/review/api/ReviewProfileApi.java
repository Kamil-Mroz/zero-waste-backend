package com.kamilpm.zero_waste.review.api;

import com.kamilpm.zero_waste.review.mapper.ReviewMapper;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.kamilpm.zero_waste.common.dto.UserSummaryDto;
import com.kamilpm.zero_waste.common.entity.ModerationStatus;
import com.kamilpm.zero_waste.common.interfaces.IRatingBreakdownWithStats;
import com.kamilpm.zero_waste.review.entity.Review;
import com.kamilpm.zero_waste.review.repository.ReviewRepository;
import com.kamilpm.zero_waste.user.api.ProfileReviewSummary;
import com.kamilpm.zero_waste.user.api.RatingBreakdown;
import com.kamilpm.zero_waste.user.api.UserReviewApi;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReviewProfileApi {
  private final ReviewMapper reviewMapper;
  private final ReviewRepository reviewRepository;
  private final UserReviewApi userReviewApi;

  public ProfileReviewSummary buildReviewSummary(UUID userId) {
    long one = 0, two = 0, three = 0, four = 0, five = 0, count = 0;
    Double avg = 0.0;

    for (IRatingBreakdownWithStats row : reviewRepository.getRatingBreakdownWithStats(userId)) {
      count = row.getTotalCount();
      avg = row.getAvgRating();
      switch (row.getRating()) {
        case 1 -> one = row.getCount();
        case 2 -> two = row.getCount();
        case 3 -> three = row.getCount();
        case 4 -> four = row.getCount();
        case 5 -> five = row.getCount();
      }
    }

    List<Review> latestReviews = reviewRepository.findTop3ByRevieweeIdAndModerationStatusOrderByCreatedAtDesc(userId,
        ModerationStatus.VISIBLE);

    Map<UUID, UserSummaryDto> usersById = userReviewApi
        .getUsersById(latestReviews.stream().map(review -> review.getReviewerId()).collect(Collectors.toSet()));

    return ProfileReviewSummary.builder()
        .averageRating(avg == null ? 0.0 : avg)
        .reviewCount(count)
        .latestReviews(latestReviews.stream()
            .map(review -> reviewMapper.toResponse(review, usersById.get(review.getReviewerId()).getNickname()))
            .toList())
        .ratingBreakdown(RatingBreakdown.builder()
            .oneStar(one)
            .twoStar(two)
            .threeStar(three)
            .fourStar(four)
            .fiveStar(five)
            .build())
        .build();
  }
}
