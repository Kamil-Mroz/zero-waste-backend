package com.kamilpm.zero_waste.service;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.kamilpm.zero_waste.domain.dto.ItemDto;
import com.kamilpm.zero_waste.domain.entity.Item;
import com.kamilpm.zero_waste.domain.entity.ItemState;
import com.kamilpm.zero_waste.domain.request.ItemRequest;
import com.kamilpm.zero_waste.domain.request.UpdateItemRequest;

public interface ItemService {
  ItemDto createItem(ItemRequest itemRequest);

  ItemDto updateItem(UUID id, UpdateItemRequest itemRequest);

  ItemDto publishItem(UUID id);

  ItemDto hideItem(UUID id);

  Page<ItemDto> getItems(Pageable pageable, String text, UUID category);

  Page<ItemDto> getOwnItems(Pageable pageable, String text, UUID category, List<ItemState> states);

  ItemDto getItem(UUID id);

  Item findByIdForUpdate(UUID id);

  void saveItem(Item item);

  void deleteItemCompletely(Item item);

  int getUserItemCount(UUID userId);

  List<ItemDto> getUserItems(UUID userId);

  void deleteItem(UUID id);

  void deleteItemsByUserIds(List<UUID> userIds);

  void deleteItemsByUser(UUID userId);

  boolean existsByCategory_Id(UUID categoryId);
}
