package com.kamilpm.zero_waste.domain.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.kamilpm.zero_waste.domain.dto.ReviewDto;
import com.kamilpm.zero_waste.domain.entity.Review;
import com.kamilpm.zero_waste.domain.response.ReviewResponse;

@Mapper(componentModel = "spring")
public interface ReviewMapper {

  @Mapping(target = "reviewerId", source = "reviewer.id")
  @Mapping(target = "reviewerName", expression = "java(review.getReviewer().getFirstName() + \" \" + review.getReviewer().getLastName())")
  ReviewResponse toResponse(Review review);

  ReviewDto toDto(Review review);

}
