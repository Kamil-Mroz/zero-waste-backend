package com.kamilpm.zero_waste.item.api;

import java.util.UUID;

import org.springframework.stereotype.Service;

import com.kamilpm.zero_waste.item.repository.ItemRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ItemCategoryApi {

  private final ItemRepository itemRepository;

  public boolean existsByCategoryId(UUID id) {
    return itemRepository.existsByCategoryId(id);
  }

}
