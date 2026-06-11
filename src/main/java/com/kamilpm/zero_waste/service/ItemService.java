package com.kamilpm.zero_waste.service;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.kamilpm.zero_waste.domain.entity.Item;
import com.kamilpm.zero_waste.domain.entity.ItemState;
import com.kamilpm.zero_waste.domain.request.ItemRequest;
import com.kamilpm.zero_waste.domain.request.UpdateItemRequest;

public interface ItemService {
  Item createItem(ItemRequest itemRequest);

  Item updateItem(UUID id, UpdateItemRequest itemRequest);

  Item publishItem(UUID id);

  Item hideItem(UUID id);

  Page<Item> getItems(Pageable pageable, String text, UUID category);

  Page<Item> getOwnItems(Pageable pageable, String text, UUID category, List<ItemState> states);

  Item getItem(UUID id);

  Item findByIdForUpdate(UUID id);

  void saveItem(Item item);

  void deleteItemCompletely(Item item);

  int getUserItemCount(UUID userId);

  List<Item> getUserItems(UUID userId);

  void deleteItem(UUID id);

  void deleteItemsByUserIds(List<UUID> userIds);

  void deleteItemsByUser(UUID userId);
}
