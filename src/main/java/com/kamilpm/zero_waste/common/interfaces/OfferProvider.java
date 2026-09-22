package com.kamilpm.zero_waste.common.interfaces;

import java.util.UUID;

import com.kamilpm.zero_waste.common.dto.SimpleOfferData;

public interface OfferProvider {

  public boolean isBuyerOfItem(UUID userId, UUID itemId);

  public SimpleOfferData getOfferById(UUID offerId);

  public SimpleOfferData getOfferByItemId(UUID itemId);
}
