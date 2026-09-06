package com.kamilpm.zero_waste.user.api;

import java.util.List;

import com.kamilpm.zero_waste.item.api.ItemCountBreakDown;
import com.kamilpm.zero_waste.item.api.ItemDto;

import lombok.Builder;

@Builder
public record ProfileItemSummary(
    ItemCountBreakDown itemCountBreakDown,
    List<ItemDto> latestItems) {
}
