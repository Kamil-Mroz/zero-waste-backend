package com.kamilpm.zero_waste.offer.api;

import java.util.UUID;

import com.kamilpm.zero_waste.offer.entity.OfferStatus;

public record SimpleOfferDto(
    UUID id,
    UUID itemId,
    UUID buyerId,
    OfferStatus status) {

}
