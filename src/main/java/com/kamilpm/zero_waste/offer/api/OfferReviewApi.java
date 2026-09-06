package com.kamilpm.zero_waste.offer.api;

import java.util.Objects;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.kamilpm.zero_waste.common.exception.EntityNotFoundException;
import com.kamilpm.zero_waste.common.exception.ForbiddenException;
import com.kamilpm.zero_waste.offer.entity.OfferStatus;
import com.kamilpm.zero_waste.offer.mapper.OfferMapper;
import com.kamilpm.zero_waste.offer.repository.OfferRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OfferReviewApi {
  private final OfferRepository offerRepository;
  private final OfferMapper offerMapper;

  public SimpleOfferDto getOfferById(UUID offerId) {
    SimpleOfferDto offer = offerRepository.findById(offerId).map(offerMapper::toSimpleDto)
        .orElseThrow(() -> new EntityNotFoundException("Offer not found"));

    if (!Objects.equals(offer.status(), OfferStatus.ACCEPTED)) {
      throw new ForbiddenException("Cannot review on unaccepted offer");
    }
    return offer;
  }

  public SimpleOfferDto getOfferByItemId(UUID itemId) {
    return offerRepository.findByItemIdAndStatus(itemId, OfferStatus.ACCEPTED).map(offerMapper::toSimpleDto)
        .orElseThrow(() -> new EntityNotFoundException("Offer not found"));
  }

}
