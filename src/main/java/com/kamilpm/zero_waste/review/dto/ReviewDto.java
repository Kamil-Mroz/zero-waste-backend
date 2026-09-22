package com.kamilpm.zero_waste.review.dto;

import java.util.UUID;

import com.kamilpm.zero_waste.common.dto.OfferData;
import com.kamilpm.zero_waste.common.entity.ModerationStatus;

public record ReviewDto(
    UUID id,
    String comment,
    int rating,
    OfferData offer,
    ModerationStatus moderationStatus) {
}
