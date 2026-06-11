package com.kamilpm.zero_waste.domain.dto;

import java.util.List;

import lombok.Builder;

@Builder
public record ProfileItemSummary(
    ItemCountBreakDown itemCountBreakDown,
    List<ItemDto> latestItems) {
}
