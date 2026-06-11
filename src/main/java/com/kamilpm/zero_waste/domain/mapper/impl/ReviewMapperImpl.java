package com.kamilpm.zero_waste.domain.mapper.impl;

import org.springframework.stereotype.Component;

import com.kamilpm.zero_waste.domain.entity.Review;
import com.kamilpm.zero_waste.domain.mapper.ReviewMapper;
import com.kamilpm.zero_waste.domain.response.ReviewResponse;

@Component
public class ReviewMapperImpl implements ReviewMapper {
  @Override
  public ReviewResponse toResponse(Review review) {
    return ReviewResponse.builder()
        .id(review.getId())
        .rating(review.getRating())
        .comment(review.getComment())
        .reviewerId(review.getReviewer().getId())
        .reviewerName(review.getReviewer().getFirstName() + " " + review.getReviewer().getLastName())
        .createdAt(review.getCreatedAt())
        .build();
  }

}
