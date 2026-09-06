package com.kamilpm.zero_waste.item.api;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.kamilpm.zero_waste.common.dto.UserSummaryDto;
import com.kamilpm.zero_waste.common.exception.EntityNotFoundException;
import com.kamilpm.zero_waste.item.entity.Item;
import com.kamilpm.zero_waste.item.mapper.ItemMapper;
import com.kamilpm.zero_waste.item.repository.ItemRepository;
import com.kamilpm.zero_waste.user.api.UserItemApi;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ItemOfferApi {
  private final ItemRepository itemRepository;
  private final ItemMapper itemMapper;
  private final UserItemApi userItemApi;

  public SimpleItemDto findByIdForUpdate(UUID itemId) {
    return itemRepository.findByIdForUpdate(itemId).map(itemMapper::toSimpleDto)
        .orElseThrow(() -> new EntityNotFoundException("Item not found"));
  }

  public SimpleItemDto findById(UUID itemId) {
    return itemRepository.findById(itemId).map(itemMapper::toSimpleDto)
        .orElseThrow(() -> new EntityNotFoundException("Item not found"));
  }

  public Map<UUID, ItemDto> getItemsByIds(Collection<UUID> ids) {
    List<Item> items = itemRepository.findAllById(ids);

    Map<UUID, UserSummaryDto> usersById = userItemApi
        .getUsersByIds(items.stream().map(item -> item.getOwnerId()).collect(Collectors.toSet()));

    return items.stream().collect(
        Collectors.toMap((item) -> item.getId(),
            (item) -> itemMapper.toDtoWithOwner(item, null, null, null, usersById.get(item.getOwnerId()))));

  }

  public Map<UUID, ItemDto> getItemsOwnedBy(UUID userId) {
    List<Item> items = itemRepository.findByOwnerId(userId);
    return items.stream().collect(
        Collectors.toMap((item) -> item.getId(),
            (item) -> itemMapper.toDto(item, null, null, null)));

  }

  public Set<UUID> findByUserIds(List<UUID> ids) {
    return itemRepository.findByOwnerIdIn(ids).stream().map(item -> item.getId()).collect(Collectors.toSet());

  }
}
