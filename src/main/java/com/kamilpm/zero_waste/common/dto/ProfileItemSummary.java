package com.kamilpm.zero_waste.common.dto;

import java.util.List;

public record ProfileItemSummary(
ItemCountBreakDown itemCountBreakDown,
List<SimpleItemData> latestItems
) {
}
