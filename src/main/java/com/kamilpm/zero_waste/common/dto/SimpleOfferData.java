package com.kamilpm.zero_waste.common.dto;

import java.util.UUID;

public record SimpleOfferData(
    UUID id,
    UUID itemId,
    UUID buyerId,
    OfferStatus status) {
}
