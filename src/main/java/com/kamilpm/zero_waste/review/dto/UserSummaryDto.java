package com.kamilpm.zero_waste.review.dto;

import java.util.UUID;

public record UserSummaryDto(
    UUID id,
    String nickname) {
}
