package com.kamilpm.zero_waste.service.impl;

import org.springframework.stereotype.Service;

import com.kamilpm.zero_waste.domain.entity.Item;
import com.kamilpm.zero_waste.repository.OfferRepository;
import com.kamilpm.zero_waste.service.OfferItemQuery;
import com.kamilpm.zero_waste.service.ReviewOfferQuery;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OfferItemQueryImpl implements OfferItemQuery {
  private final ReviewOfferQuery reviewService;
  private final OfferRepository offerRepository;

  @Override
  public void deleteAllByItem(Item item) {
    reviewService.deleteByItem(item);
    offerRepository.deleteAllByItem_Id(item.getId());
  }

}
