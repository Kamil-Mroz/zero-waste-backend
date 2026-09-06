package com.kamilpm.zero_waste.item.api;

import lombok.Builder;

@Builder
public record ItemCountBreakDown(
    long totalItems,
    long given,
    long pending,
    long available) {

}
