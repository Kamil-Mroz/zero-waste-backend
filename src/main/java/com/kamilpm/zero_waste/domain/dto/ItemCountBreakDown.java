package com.kamilpm.zero_waste.domain.dto;

import lombok.Builder;

@Builder
public record ItemCountBreakDown(
    long totalItems,
    long given,
    long pending,
    long available) {

}
