package com.kamilpm.zero_waste.review.dto;

import java.util.UUID;

import com.kamilpm.zero_waste.common.dto.OfferStatus;
import com.kamilpm.zero_waste.common.dto.SimpleItemData;
import com.kamilpm.zero_waste.common.dto.UserSummaryDto;

public record OfferDto(

    UUID id,
    SimpleItemData item,
    UserSummaryDto buyer,
    OfferStatus status) {

}
