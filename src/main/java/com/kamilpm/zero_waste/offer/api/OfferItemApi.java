package com.kamilpm.zero_waste.offer.api;

import java.util.UUID;

import org.springframework.stereotype.Service;

import com.kamilpm.zero_waste.offer.repository.OfferRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OfferItemApi {
  private final OfferRepository offerRepository;

  public boolean isBuyerOfItem(UUID userId, UUID itemId) {
    return offerRepository.existsByBuyerIdAndItemId(userId, itemId);

  }

}
