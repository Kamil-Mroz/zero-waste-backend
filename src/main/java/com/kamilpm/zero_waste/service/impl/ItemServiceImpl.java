package com.kamilpm.zero_waste.service.impl;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.kamilpm.zero_waste.domain.entity.Category;
import com.kamilpm.zero_waste.domain.entity.Item;
import com.kamilpm.zero_waste.domain.entity.ItemState;
import com.kamilpm.zero_waste.domain.entity.User;
import com.kamilpm.zero_waste.domain.request.ItemRequest;
import com.kamilpm.zero_waste.exception.EntityNotFoundException;
import com.kamilpm.zero_waste.exception.ForbiddenException;
import com.kamilpm.zero_waste.repository.ItemRepository;
import com.kamilpm.zero_waste.service.AuthService;
import com.kamilpm.zero_waste.service.CategoryService;
import com.kamilpm.zero_waste.service.ItemService;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Transactional
@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {

  private final CategoryService categoryService;
  private final ItemRepository itemRepository;
  private final AuthService authService;

  @Override
  public Item createItem(ItemRequest itemRequest) {
    User user = authService.getRequiredAuthenticatedUser();

    Category category = categoryService.getCategoryById(itemRequest.getCategoryId());

    Item item = Item.builder()
        .title(itemRequest.getTitle())
        .description(itemRequest.getDescription())
        .condition(itemRequest.getCondition())
        .state(ItemState.AVAILABLE)
        .city(itemRequest.getCity())
        .category(category)
        .owner(user)
        .build();

    return itemRepository.save(item);

  }

  @Override
  public Item updateItem(UUID id, ItemRequest itemRequest) {
    Category category = categoryService.getCategoryById(itemRequest.getCategoryId());

    Item item = itemRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Item not found"));

    User user = authService.getRequiredAuthenticatedUser();

    if (!Objects.equals(item.getOwner().getId(), user.getId())) {
      throw new ForbiddenException("Must be the owner of the item to update it");
    }

    if (Objects.equals(ItemState.GIVEN, item.getState())) {
      throw new ForbiddenException("Can not update a given item");
    }

    item.setTitle(itemRequest.getTitle());
    item.setDescription(itemRequest.getDescription());
    item.setCondition(itemRequest.getCondition());
    item.setCity(itemRequest.getCity());
    item.setCategory(category);

    return itemRepository.save(item);
  }

  @Override
  public List<Item> getItems() {
    Optional<User> user = authService.getAuthenticatedUser();
    if (user.isPresent()) {
      return itemRepository.findByOwner_IdNotAndStateNot(user.get().getId(), ItemState.GIVEN);
    }
    return itemRepository.findByStateNot(ItemState.GIVEN);
  }

  @Override
  public Item getItem(UUID id) {
    Item item = itemRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Item not found"));

    Optional<User> user = authService.getAuthenticatedUser();

    if (Objects.equals(ItemState.GIVEN, item.getState())) {

      if (!user.isPresent()) {
        throw new EntityNotFoundException("Item not found");
      }

      // if (!Objects.equals(item.getOwner().getId(), user.get().getId()) ||
      // !Objects.equals(, user.get().getId())) {

      // throw new EntityNotFoundException("Item not found");
      // }

    }

    return item;
  }

  @Override
  public List<Item> getOwnItems() {
    User user = authService.getRequiredAuthenticatedUser();
    return itemRepository.findByOwner_Id(user.getId());
  }

}
