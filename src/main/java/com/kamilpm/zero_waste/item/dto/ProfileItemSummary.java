package com.kamilpm.zero_waste.item.dto;

import java.util.List;

public record ProfileItemSummary(
    ItemCountBreakDown itemCountBreakDown,
    List<ItemDto> latestItems) {
}
