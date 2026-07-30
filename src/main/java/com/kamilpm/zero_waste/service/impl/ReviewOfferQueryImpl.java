package com.kamilpm.zero_waste.service.impl;

import org.springframework.stereotype.Service;

import com.kamilpm.zero_waste.domain.entity.Item;
import com.kamilpm.zero_waste.repository.ReviewRepository;
import com.kamilpm.zero_waste.service.ReviewOfferQuery;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReviewOfferQueryImpl implements ReviewOfferQuery {

  private final ReviewRepository reviewRepository;

  @Override
  public void deleteByItem(Item item) {
    reviewRepository.deleteByRevieweeIdAndItemId(item.getOwner().getId(), item.getId());
  }

}
