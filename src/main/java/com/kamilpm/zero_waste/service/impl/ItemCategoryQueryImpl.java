package com.kamilpm.zero_waste.service.impl;

import java.util.UUID;

import org.springframework.stereotype.Service;

import com.kamilpm.zero_waste.repository.ItemRepository;
import com.kamilpm.zero_waste.service.ItemCategoryQuery;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ItemCategoryQueryImpl implements ItemCategoryQuery {

  private final ItemRepository itemRepository;

  @Override
  public boolean existsByCategory_Id(UUID id) {
    return itemRepository.existsByCategory_Id(id);
  }

}
