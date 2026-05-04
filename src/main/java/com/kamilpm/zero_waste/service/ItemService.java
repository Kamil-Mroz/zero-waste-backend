package com.kamilpm.zero_waste.service;

import java.util.List;
import java.util.UUID;

import com.kamilpm.zero_waste.domain.entity.Item;
import com.kamilpm.zero_waste.domain.request.ItemRequest;
import com.kamilpm.zero_waste.domain.request.UpdateItemRequest;

public interface ItemService {
  Item createItem(ItemRequest itemRequest);

  Item updateItem(UUID id, UpdateItemRequest itemRequest);

  List<Item> getItems();

  List<Item> getOwnItems();

  Item getItem(UUID id);

  void deleteItem(UUID id);
}
