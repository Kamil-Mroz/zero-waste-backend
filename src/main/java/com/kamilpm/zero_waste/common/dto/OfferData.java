package com.kamilpm.zero_waste.common.dto;

import java.util.UUID;

public record OfferData(
    UUID id,
    SimpleItemData item,
    UserSummaryDto buyer,
    OfferStatus status) {
}
