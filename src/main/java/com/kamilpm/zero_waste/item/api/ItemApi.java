package com.kamilpm.zero_waste.item.api;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import com.kamilpm.zero_waste.common.dto.ItemCountBreakDown;
import com.kamilpm.zero_waste.common.dto.ItemState;
import com.kamilpm.zero_waste.common.dto.ProfileItemSummary;
import com.kamilpm.zero_waste.common.dto.SimpleItemData;
import com.kamilpm.zero_waste.common.dto.UserSummaryDto;
import com.kamilpm.zero_waste.common.entity.ModerationStatus;
import com.kamilpm.zero_waste.common.events.DeleteItemEvent;
import com.kamilpm.zero_waste.common.exception.EntityNotFoundException;
import com.kamilpm.zero_waste.common.exception.ForbiddenException;
import com.kamilpm.zero_waste.common.interfaces.ItemProvider;
import com.kamilpm.zero_waste.common.interfaces.UserProvider;
import com.kamilpm.zero_waste.item.entity.Item;
import com.kamilpm.zero_waste.item.mapper.ItemMapper;
import com.kamilpm.zero_waste.item.repository.ItemRepository;
import com.kamilpm.zero_waste.item.service.ItemService;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ItemApi implements ItemProvider {
  private final ItemRepository itemRepository;
  private final ItemMapper itemMapper;
  private final UserProvider userApi;
  private final ApplicationEventPublisher events;
  private final ItemService itemService;

  public SimpleItemData getItemById(UUID itemId) {

    SimpleItemData item = itemRepository.findById(itemId).map(itemMapper::toSimpleItemData)
        .orElseThrow(() -> new EntityNotFoundException("Item not found"));

    if (!Objects.equals(item.state(), ItemState.GIVEN)) {
      throw new ForbiddenException("Cannot review on non give item");
    }
    return item;
  }

  public boolean existsByCategoryId(UUID id) {
    return itemRepository.existsByCategoryId(id);
  }

  public SimpleItemData findByIdForUpdate(UUID itemId) {
    return itemRepository.findByIdForUpdate(itemId).map(itemMapper::toSimpleItemData)
        .orElseThrow(() -> new EntityNotFoundException("Item not found"));
  }

  public SimpleItemData findById(UUID itemId) {
    return itemRepository.findById(itemId).map(itemMapper::toSimpleItemData)
        .orElseThrow(() -> new EntityNotFoundException("Item not found"));
  }

  public Map<UUID, SimpleItemData> getItemsByIds(Collection<UUID> ids) {
    List<Item> items = itemRepository.findAllById(ids);

    Set<UUID> ownerIds = items.stream().map(item -> item.getOwnerId()).collect(Collectors.toSet());
    Map<UUID, UserSummaryDto> usersById = userApi.getUserSummaryByIds(ownerIds);

    return items.stream().collect(
        Collectors.toMap((item) -> item.getId(),
            (item) -> itemMapper.toSimpleItemData(item, usersById.get(item.getOwnerId()))));

  }

  public Map<UUID, SimpleItemData> getItemsOwnedBy(UUID userId) {
    List<Item> items = itemRepository.findByOwnerId(userId);
    return items.stream().collect(
        Collectors.toMap((item) -> item.getId(),
            (item) -> itemMapper.toSimpleItemData(item)));

  }

  public Set<UUID> findByUserIds(List<UUID> ids) {
    return itemRepository.findByOwnerIdIn(ids).stream().map(item -> item.getId()).collect(Collectors.toSet());

  }

  public ProfileItemSummary buildItemSummary(UUID userId) {
    List<Item> latestItems = itemRepository.findTop3ByOwnerIdAndStateAndModerationStatusOrderByCreatedAtDesc(userId,
        ItemState.AVAILABLE, ModerationStatus.VISIBLE);

    ItemCountBreakDown itemCountBreakDown = buildItemCountBreakDown(userId);
    return new ProfileItemSummary(
        itemCountBreakDown,
        latestItems.stream().map(itemMapper::toSimpleItemData).toList());
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

  public void itemExists(UUID subjectId, UUID userId) {
    Item item = itemRepository.findById(subjectId).orElseThrow(() -> new EntityNotFoundException("Item not found"));

    if (Objects.equals(item.getOwnerId(), userId))
      throw new ForbiddenException("You can not report yourself");

    if (userApi.isUserDemo(item.getOwnerId()))
      throw new ForbiddenException("Unable to interact with demo users");

    if (Objects.equals(item.getModerationStatus(), ModerationStatus.HIDDEN))
      throw new ForbiddenException("Unable to report a hidden item");

    if (!Objects.equals(item.getState(), ItemState.AVAILABLE))
      throw new ForbiddenException("Only available items can be reported");

  }

  public boolean isItemOwner(UUID itemId, UUID userId) {
    return itemRepository.existsByIdAndOwnerId(itemId, userId);
  }

  @Transactional
  public void deleteItemById(UUID itemId) {
    itemService.deleteItemCompletely(itemRepository.findById(itemId).orElse(null));
    events.publishEvent(new DeleteItemEvent(itemId));
  }

  public void hideItem(UUID adminId, UUID subjectId) {
    Item item = itemRepository.findByIdAndModerationStatus(subjectId, ModerationStatus.VISIBLE)
        .orElseThrow(() -> new EntityNotFoundException("Item not found"));
    item.setModeratedAt(Instant.now());
    item.setModeratedBy(adminId);
    item.setModerationStatus(ModerationStatus.HIDDEN);
    itemRepository.save(item);
  }

}
