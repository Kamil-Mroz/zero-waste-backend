package com.kamilpm.zero_waste.service.impl;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kamilpm.zero_waste.domain.dto.ItemDto;
import com.kamilpm.zero_waste.domain.entity.Category;
import com.kamilpm.zero_waste.domain.entity.Image;
import com.kamilpm.zero_waste.domain.entity.Item;
import com.kamilpm.zero_waste.domain.entity.ItemState;
import com.kamilpm.zero_waste.domain.entity.User;
import com.kamilpm.zero_waste.domain.mapper.ItemMapper;
import com.kamilpm.zero_waste.domain.request.ItemRequest;
import com.kamilpm.zero_waste.domain.request.UpdateItemRequest;
import com.kamilpm.zero_waste.exception.ConflictException;
import com.kamilpm.zero_waste.exception.EntityNotFoundException;
import com.kamilpm.zero_waste.exception.ForbiddenException;
import com.kamilpm.zero_waste.repository.ItemRepository;
import com.kamilpm.zero_waste.service.AuthService;
import com.kamilpm.zero_waste.service.CategoryService;
import com.kamilpm.zero_waste.service.ImageService;
import com.kamilpm.zero_waste.service.ItemOwnershipService;
import com.kamilpm.zero_waste.service.ItemService;
import com.kamilpm.zero_waste.utils.SqlUtils;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {

  private final CategoryService categoryService;
  private final ItemRepository itemRepository;
  private final AuthService authService;
  private final ImageService imageService;
  private final ItemOwnershipService itemOwnershipService;
  private final ItemMapper itemMapper;

  @Override
  @Transactional
  public ItemDto createItem(ItemRequest itemRequest) {
    User user = authService.getRequiredAuthenticatedUser();

    if (itemRequest.getImages().size() > 5) {
      throw new ConflictException("Max image count is 5", "images");
    }

    if (Objects.equals(itemRequest.getState(), ItemState.GIVEN)) {
      throw new ConflictException("Unable to create a given item");
    }

    Category category = categoryService.getCategoryById(itemRequest.getCategoryId());

    Item item = Item.builder()
        .title(itemRequest.getTitle())
        .description(itemRequest.getDescription())
        .condition(itemRequest.getCondition())
        .state(itemRequest.getState())
        .city(itemRequest.getCity())
        .category(category)
        .owner(user)
        .build();

    Item savedItem = itemRepository.save(item);
    imageService.uploadItemImages(savedItem, itemRequest.getImages());
    return itemMapper.toDto(savedItem);
  }

  @Override
  @Transactional
  public ItemDto updateItem(UUID id, UpdateItemRequest itemRequest) {

    if (Objects.equals(itemRequest.getState(), ItemState.GIVEN)) {
      throw new ConflictException("Unable to update to a given item");
    }

    Category category = categoryService.getCategoryById(itemRequest.getCategoryId());

    Item item = itemRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Item not found"));

    User user = authService.getRequiredAuthenticatedUser();

    if (!Objects.equals(item.getOwner().getId(), user.getId())) {
      throw new ForbiddenException("Must be the owner of the item to update it");
    }

    if (Objects.equals(ItemState.GIVEN, item.getState())) {
      throw new ForbiddenException("Can not update a given item");
    }
    Set<UUID> itemImageIds = item.getImages().stream().map(Image::getId).collect(Collectors.toSet());

    List<UUID> validIdsToRemove = itemRequest.getRemovedImageIds().stream().filter(itemImageIds::contains).toList();

    if (validIdsToRemove.size() != itemRequest.getRemovedImageIds().size()) {
      throw new ForbiddenException("Some images do not belong to this item");
    }

    if ((item.getImages().size() - validIdsToRemove.size() + itemRequest.getImages().size()) > 5) {
      throw new ConflictException("Max image count is 5", "images");
    }

    item.setTitle(itemRequest.getTitle());
    item.setDescription(itemRequest.getDescription());
    item.setCondition(itemRequest.getCondition());
    item.setCity(itemRequest.getCity());
    item.setCategory(category);
    item.setState(itemRequest.getState());

    imageService.deleteImages(item.getId(), validIdsToRemove);
    imageService.uploadItemImages(item, itemRequest.getImages());

    Item updatedItem = itemRepository.save(item);
    return itemMapper.toDto(updatedItem);

  }

  @Override
  @Transactional(readOnly = true)
  public Page<ItemDto> getItems(Pageable pageable, String text, UUID category) {
    text = SqlUtils.prepareLikePattern(text);
    Set<UUID> categoryIds = null;
    if (category != null) {
      categoryIds = categoryService.getCategoryDescendantsCache().get(category);
    }
    Optional<User> user = authService.getAuthenticatedUser();
    UUID excludeOwnerId = user.map(User::getId).orElse(null);

    return itemRepository.searchItems(excludeOwnerId, ItemState.AVAILABLE, text, categoryIds, pageable)
        .map(itemMapper::toDto);
  }

  @Override
  @Transactional(readOnly = true)
  public ItemDto getItem(UUID id) {
    Item item = itemRepository.findByIdWithOwnerAndCategoryAndImages(id)
        .orElseThrow(() -> new EntityNotFoundException("Item not found"));

    if (Objects.equals(item.getState(), ItemState.AVAILABLE))
      return itemMapper.toDtoWithOwner(item);

    User user = authService.getRequiredAuthenticatedUser();
    UUID userId = user.getId();

    if (userId.equals(item.getOwner().getId()))
      return itemMapper.toDtoWithOwner(item);

    if (itemOwnershipService.isBuyerOfItem(userId, item.getId()))
      return itemMapper.toDtoWithOwner(item);

    throw new EntityNotFoundException("Item not available");

  }

  @Override
  public Item findByIdForUpdate(UUID id) {
    return itemRepository.findByIdForUpdate(id).orElseThrow(() -> new EntityNotFoundException("Item not found"));
  }

  @Override
  public void saveItem(Item item) {
    itemRepository.save(item);
  }

  @Override
  @Transactional(readOnly = true)
  public Page<ItemDto> getOwnItems(Pageable pageable, String text, UUID category, List<ItemState> states) {
    User user = authService.getRequiredAuthenticatedUser();
    text = SqlUtils.prepareLikePattern(text);
    if (states == null || states.size() == 0)
      states = List.of(ItemState.AVAILABLE, ItemState.PENDING);
    Set<UUID> categoryIds = null;
    if (category != null) {
      categoryIds = categoryService.getCategoryDescendantsCache().get(category);
    }
    return itemRepository.findOwnItems(user.getId(), text, categoryIds, states, pageable).map(itemMapper::toDto);
  }

  @Override
  @Transactional
  public void deleteItem(UUID id) {
    User user = authService.getRequiredAuthenticatedUser();
    Item item = itemRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Item not found"));

    if (!Objects.equals(user.getId(), item.getOwner().getId())) {
      throw new ForbiddenException("Others items can not be deleted");
    }
    if (Objects.equals(item.getState(), ItemState.GIVEN)) {
      throw new ForbiddenException("Given item can not be deleted");
    }

    deleteItemCompletely(item);

  }

  @Override
  @Transactional
  public ItemDto publishItem(UUID id) {
    Item item = itemRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Item not found"));

    User user = authService.getRequiredAuthenticatedUser();

    if (!Objects.equals(item.getOwner().getId(), user.getId())) {
      throw new ForbiddenException("Must be the owner of the item to update it");
    }
    if (!Objects.equals(item.getState(), ItemState.PENDING)) {
      throw new ForbiddenException("Unable to publish a non pending item");
    }

    item.setState(ItemState.AVAILABLE);

    Item publishedItem = itemRepository.save(item);
    return itemMapper.toDto(publishedItem);
  }

  @Override
  @Transactional
  public ItemDto hideItem(UUID id) {
    Item item = itemRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Item not found"));

    User user = authService.getRequiredAuthenticatedUser();

    if (!Objects.equals(item.getOwner().getId(), user.getId())) {
      throw new ForbiddenException("Must be the owner of the item to update it");
    }
    if (!Objects.equals(item.getState(), ItemState.AVAILABLE)) {
      throw new ForbiddenException("Unable to hide a non available item");
    }

    item.setState(ItemState.PENDING);

    Item hiddenItem = itemRepository.save(item);
    return itemMapper.toDto(hiddenItem);
  }

  @Override
  @Transactional
  public void deleteItemCompletely(Item item) {
    imageService.deleteImagesFromDisk(item.getImages());
    imageService.deleteImagesByItemId(item.getId());
    itemRepository.delete(item);
  }

  @Override
  @Transactional
  public void deleteItemsByUser(UUID userId) {
    List<Item> items = itemRepository.findByOwner_id(userId);
    for (Item item : items) {
      imageService.deleteImagesFromDisk(item.getImages());

      imageService.deleteImagesByItemId(item.getId());
    }
    itemRepository.deleteAll(items);

  }

  @Override
  @Transactional
  public void deleteItemsByUserIds(List<UUID> userIds) {
    List<Item> items = itemRepository.findByOwnerIdIn(userIds);
    for (Item item : items) {
      imageService.deleteImagesFromDisk(item.getImages());

      imageService.deleteImagesByItemId(item.getId());
    }

    itemRepository.deleteAll(items);

  }

  @Override
  @Transactional(readOnly = true)
  public int getUserItemCount(UUID userId) {
    return itemRepository.countByOwner_Id(userId);
  }

  @Override
  @Transactional(readOnly = true)
  public List<ItemDto> getUserItems(UUID userId) {

    return itemRepository.findByOwner_IdAndState(userId, ItemState.AVAILABLE).stream().map(itemMapper::toDto).toList();
  }

}
