package com.kamilpm.zero_waste.review.mapper;

import org.springframework.stereotype.Component;

import com.kamilpm.zero_waste.offer.api.OfferDto;
import com.kamilpm.zero_waste.review.api.ReviewResponse;
import com.kamilpm.zero_waste.review.dto.ReviewDto;
import com.kamilpm.zero_waste.review.entity.Review;

@Component
public class ReviewMapper {

  public ReviewResponse toResponse(Review review, String reviewerName) {

    if (review == null)
      return null;
    return new ReviewResponse(review.getId(), review.getRating(), review.getComment(), review.getReviewerId(),
        review.getRevieweeId(), reviewerName, review.getModerationStatus(), review.getCreatedAt());
  };

  public ReviewDto toDto(Review review, OfferDto offer) {
    if (review == null)
      return null;
    return new ReviewDto(review.getId(), review.getComment(), review.getRating(), offer, review.getModerationStatus());

  };

}
