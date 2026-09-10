package com.kamilpm.zero_waste.review.dto;

import java.util.UUID;

public record SimpleOfferDto(
    UUID id,
    UUID itemId,
    UUID buyerId,
    OfferStatus status) {

}
