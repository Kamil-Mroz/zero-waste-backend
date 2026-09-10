package com.kamilpm.zero_waste.blog.dto;

import java.util.UUID;

public record UserSummaryDto(
    UUID id,
    String nickname) {
}
