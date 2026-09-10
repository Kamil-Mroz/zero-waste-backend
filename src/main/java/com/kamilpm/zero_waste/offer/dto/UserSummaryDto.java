package com.kamilpm.zero_waste.offer.dto;

import java.util.UUID;

public record UserSummaryDto(
    UUID id,
    String nickname) {
}
