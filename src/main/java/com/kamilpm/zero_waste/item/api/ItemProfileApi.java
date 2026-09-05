package com.kamilpm.zero_waste.item.api;

import com.kamilpm.zero_waste.item.mapper.ItemMapper;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.kamilpm.zero_waste.common.entity.ModerationStatus;
import com.kamilpm.zero_waste.item.entity.Item;
import com.kamilpm.zero_waste.item.repository.ItemRepository;
import com.kamilpm.zero_waste.user.api.ProfileItemSummary;

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
    return ProfileItemSummary.builder()
        .latestItems(latestItems.stream().map(item -> itemMapper.toDto(item, null, null, null)).toList())
        .itemCountBreakDown(itemCountBreakDown).build();
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
    return ItemCountBreakDown.builder()
        .available(available)
        .pending(pending)
        .given(given)
        .totalItems(available + pending + given)
        .build();

  }

}
