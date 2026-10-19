package com.kamilpm.zero_waste.offer.dto;

import java.util.UUID;

import com.kamilpm.zero_waste.common.dto.OfferStatus;
import com.kamilpm.zero_waste.common.dto.SimpleItemData;
import com.kamilpm.zero_waste.common.dto.UserSummaryWithEmailDto;

public record OfferWithEmailDto(
    UUID id,
    SimpleItemData item,
    UserSummaryWithEmailDto buyer,
    OfferStatus status) {
}
