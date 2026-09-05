package com.kamilpm.zero_waste.review.dto;

import java.util.UUID;

import com.kamilpm.zero_waste.common.entity.ModerationStatus;
import com.kamilpm.zero_waste.offer.api.OfferDto;

public record ReviewDto(
    UUID id,
    String comment,
    int rating,
    OfferDto offer,
    ModerationStatus moderationStatus) {
}
