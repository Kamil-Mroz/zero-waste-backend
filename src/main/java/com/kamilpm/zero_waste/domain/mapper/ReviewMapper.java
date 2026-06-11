package com.kamilpm.zero_waste.domain.mapper;

import com.kamilpm.zero_waste.domain.entity.Review;
import com.kamilpm.zero_waste.domain.response.ReviewResponse;

public interface ReviewMapper {

  ReviewResponse toResponse(Review review);

}
