package com.kamilpm.zero_waste.offer.api;

import java.util.Objects;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.kamilpm.zero_waste.common.dto.OfferStatus;
import com.kamilpm.zero_waste.common.dto.SimpleOfferData;
import com.kamilpm.zero_waste.common.exception.EntityNotFoundException;
import com.kamilpm.zero_waste.common.exception.ForbiddenException;
import com.kamilpm.zero_waste.common.interfaces.OfferProvider;
import com.kamilpm.zero_waste.offer.mapper.OfferMapper;
import com.kamilpm.zero_waste.offer.repository.OfferRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OfferApi implements OfferProvider {
  private final OfferRepository offerRepository;
  private final OfferMapper offerMapper;

  public boolean isBuyerOfItem(UUID userId, UUID itemId) {
    return offerRepository.existsByBuyerIdAndItemId(userId, itemId);

  }

  public SimpleOfferData getOfferById(UUID offerId) {
    SimpleOfferData offer = offerRepository.findById(offerId).map(offerMapper::toSimpleDto)
        .orElseThrow(() -> new EntityNotFoundException("Offer not found"));

    if (!Objects.equals(offer.status(), OfferStatus.ACCEPTED)) {
      throw new ForbiddenException("Cannot review on unaccepted offer");
    }
    return offer;
  }

  public SimpleOfferData getOfferByItemId(UUID itemId) {
    return offerRepository.findByItemIdAndStatus(itemId, OfferStatus.ACCEPTED).map(offerMapper::toSimpleDto)
        .orElseThrow(() -> new EntityNotFoundException("Offer not found"));
  }

}
