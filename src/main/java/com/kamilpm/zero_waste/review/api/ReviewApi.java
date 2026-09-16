package com.kamilpm.zero_waste.review.api;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.kamilpm.zero_waste.common.dto.ProfileReviewSummary;
import com.kamilpm.zero_waste.common.dto.RatingBreakdown;
import com.kamilpm.zero_waste.common.dto.UserSummaryDto;
import com.kamilpm.zero_waste.common.entity.ModerationStatus;
import com.kamilpm.zero_waste.common.exception.EntityNotFoundException;
import com.kamilpm.zero_waste.common.exception.ForbiddenException;
import com.kamilpm.zero_waste.common.interfaces.ReviewProvider;
import com.kamilpm.zero_waste.common.interfaces.UserProvider;
import com.kamilpm.zero_waste.review.entity.Review;
import com.kamilpm.zero_waste.review.interfaces.IRatingBreakdownWithStats;
import com.kamilpm.zero_waste.review.mapper.ReviewMapper;
import com.kamilpm.zero_waste.review.repository.ReviewRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReviewApi implements ReviewProvider {
  private final ReviewRepository reviewRepository;
  private final ReviewMapper reviewMapper;
  private final UserProvider userApi;

  public void reviewExists(UUID subjectId, UUID userId) {
    Review review = reviewRepository.findById(subjectId)
        .orElseThrow(() -> new EntityNotFoundException("Review not found"));

    if (Objects.equals(review.getReviewerId(), userId))
      throw new ForbiddenException("You can not report yourself");

    if (!Objects.equals(review.getRevieweeId(), userId))
      throw new ForbiddenException("You can not report review not received");

    if (Objects.equals(review.getModerationStatus(), ModerationStatus.HIDDEN))
      throw new ForbiddenException("Unable to report a hidden review");

  }

  public boolean isReviewerOrReviewee(UUID reviewId, UUID userId) {
    return reviewRepository.isReviewerOrReviewee(
        reviewId,
        userId);
  }

  public void deleteReviewById(UUID reviewId) {
    reviewRepository.deleteById(reviewId);
  }

  public void hideReview(UUID adminId, UUID subjectId) {

    Review review = reviewRepository.findByIdAndModerationStatus(subjectId, ModerationStatus.VISIBLE)
        .orElseThrow(() -> new EntityNotFoundException("Review not found"));
    review.setModeratedAt(Instant.now());
    review.setModeratedBy(adminId);
    review.setModerationStatus(ModerationStatus.HIDDEN);
    reviewRepository.save(review);

  }

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
    Set<UUID> userIds = latestReviews.stream().map(review -> review.getReviewerId()).collect(Collectors.toSet());
    Map<UUID, UserSummaryDto> usersById = userApi.getUserSummaryByIds(userIds);

    return ProfileReviewSummary.builder()
        .averageRating(avg == null ? 0.0 : avg)
        .reviewCount(count)
        .latestReviews(latestReviews.stream()
            .map(review -> reviewMapper.toResponse(review, usersById.get(review.getReviewerId()).nickname()))
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
