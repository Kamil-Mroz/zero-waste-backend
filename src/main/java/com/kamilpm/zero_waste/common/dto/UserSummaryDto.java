package com.kamilpm.zero_waste.common.dto;

import java.util.UUID;

public record UserSummaryDto(
    UUID id,
    String nickname) {
}
