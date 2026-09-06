package com.kamilpm.zero_waste.item.api;

import java.util.Objects;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.kamilpm.zero_waste.common.exception.ForbiddenException;
import com.kamilpm.zero_waste.item.mapper.ItemMapper;
import com.kamilpm.zero_waste.item.repository.ItemRepository;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ItemReviewApi {
  private final ItemRepository itemRepository;
  private final ItemMapper itemMapper;

  public SimpleItemDto getItemById(UUID itemId) {

    SimpleItemDto item = itemRepository.findById(itemId).map(itemMapper::toSimpleDto)
        .orElseThrow(() -> new EntityNotFoundException("Item not found"));

    if (!Objects.equals(item.state(), ItemState.GIVEN)) {
      throw new ForbiddenException("Cannot review on non give item");
    }
    return item;
  }

}
