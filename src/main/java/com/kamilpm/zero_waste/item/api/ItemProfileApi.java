package com.kamilpm.zero_waste.item.api;

import com.kamilpm.zero_waste.item.mapper.ItemMapper;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.kamilpm.zero_waste.common.entity.ModerationStatus;
import com.kamilpm.zero_waste.item.dto.ItemCountBreakDown;
import com.kamilpm.zero_waste.item.dto.ItemState;
import com.kamilpm.zero_waste.item.dto.ProfileItemSummary;
import com.kamilpm.zero_waste.item.entity.Item;
import com.kamilpm.zero_waste.item.repository.ItemRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ItemProfileApi {
  private final ItemMapper itemMapper;
  private final ItemRepository itemRepository;

  public ProfileItemSummary buildItemSummary(UUID userId) {
    List<Item> latestItems = itemRepository.findTop3ByOwnerIdAndStateAndModerationStatusOrderByCreatedAtDesc(userId,
        ItemState.AVAILABLE, ModerationStatus.VISIBLE);

    ItemCountBreakDown itemCountBreakDown = buildItemCountBreakDown(userId);
    return new ProfileItemSummary(
        itemCountBreakDown,
        latestItems.stream().map(item -> itemMapper.toDto(item, null, null, null)).toList());
  }

  private ItemCountBreakDown buildItemCountBreakDown(UUID userId) {
    long given = 0, available = 0, pending = 0;
    for (var row : itemRepository.countTotalItemsByOwnerIdAndState(userId)) {
      switch (row.getItemState()) {
        case GIVEN -> given = row.getTotalItem();
        case AVAILABLE -> available = row.getTotalItem();
        case PENDING -> pending = row.getTotalItem();
      }
    }
    return new ItemCountBreakDown(
        available + pending + given,
        given,
        pending,
        available);
  }
}
