package com.kamilpm.zero_waste.domain.dto;

import java.util.UUID;

import com.kamilpm.zero_waste.domain.entity.Offer;

public class ReviewDto {
  UUID id;
  String comment;
  int rating;
  Offer offer;
}
