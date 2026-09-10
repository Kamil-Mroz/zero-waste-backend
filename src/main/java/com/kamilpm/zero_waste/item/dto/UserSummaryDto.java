package com.kamilpm.zero_waste.item.dto;

import java.util.UUID;

public record UserSummaryDto(
    UUID id,
    String nickname) {
}
