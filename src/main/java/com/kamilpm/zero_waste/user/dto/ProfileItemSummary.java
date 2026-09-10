package com.kamilpm.zero_waste.user.dto;

import java.util.List;

public record ProfileItemSummary(
    ItemCountBreakDown itemCountBreakDown,
    List<ItemDto> latestItems) {
}
