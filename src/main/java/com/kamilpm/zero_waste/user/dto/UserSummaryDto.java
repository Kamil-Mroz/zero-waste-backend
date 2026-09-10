package com.kamilpm.zero_waste.user.dto;

import java.util.UUID;

public record UserSummaryDto(
    UUID id,
    String nickname) {
}
