package com.kamilpm.zero_waste.service.impl;

import com.kamilpm.zero_waste.repository.ImageRepository;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.kamilpm.zero_waste.domain.entity.Category;
import com.kamilpm.zero_waste.domain.entity.Image;
import com.kamilpm.zero_waste.domain.entity.Item;
import com.kamilpm.zero_waste.domain.entity.ItemState;
import com.kamilpm.zero_waste.domain.entity.User;
import com.kamilpm.zero_waste.domain.request.ItemRequest;
import com.kamilpm.zero_waste.domain.request.UpdateItemRequest;
import com.kamilpm.zero_waste.exception.ConflictException;
import com.kamilpm.zero_waste.exception.EntityNotFoundException;
import com.kamilpm.zero_waste.exception.ForbiddenException;
import com.kamilpm.zero_waste.repository.ItemRepository;
import com.kamilpm.zero_waste.repository.OfferRepository;
import com.kamilpm.zero_waste.security.MyUserDetails;
import com.kamilpm.zero_waste.service.AuthService;
import com.kamilpm.zero_waste.service.CategoryService;
import com.kamilpm.zero_waste.service.ImageService;
import com.kamilpm.zero_waste.service.ItemService;
import com.kamilpm.zero_waste.utils.SqlUtils;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Transactional
@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {

  private final ImageRepository imageRepository;
  private final CategoryService categoryService;
  private final ItemRepository itemRepository;
  private final AuthService authService;
  private final ImageService imageService;
  private final OfferRepository offerRepository;

  @Override
  public Item createItem(ItemRequest itemRequest) {
    User user = authService.getRequiredAuthenticatedUserEntity();

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
    return savedItem;
  }

  @Override
  public Item updateItem(UUID id, UpdateItemRequest itemRequest) {

    if (Objects.equals(itemRequest.getState(), ItemState.GIVEN)) {
      throw new ConflictException("Unable to update to a given item");
    }

    Category category = categoryService.getCategoryById(itemRequest.getCategoryId());

    Item item = itemRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Item not found"));

    MyUserDetails user = authService.getRequiredAuthenticatedUserDetails();

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

    return itemRepository.save(item);
  }

  @Override
  public Page<Item> getItems(Pageable pageable, String text, UUID category) {
    text = SqlUtils.prepareLikePattern(text);
    Set<UUID> categoryIds = null;
    if (category != null) {
      categoryIds = categoryService.getCategoryDescendantsCache().get(category);
    }
    Optional<MyUserDetails> user = authService.getAuthenticatedUser();
    UUID excludeOwnerId = user.map(MyUserDetails::getId).orElse(null);

    return itemRepository.searchItems(excludeOwnerId, ItemState.AVAILABLE, text, categoryIds, pageable);
  }

  @Override
  public Item getItem(UUID id) {
    Item item = itemRepository.findByIdWithOwnerAndCategoryAndImages(id)
        .orElseThrow(() -> new EntityNotFoundException("Item not found"));

    if (Objects.equals(item.getState(), ItemState.AVAILABLE))
      return item;

    MyUserDetails user = authService.getRequiredAuthenticatedUserDetails();
    UUID userId = user.getId();

    if (userId.equals(item.getOwner().getId()))
      return item;

    if (offerRepository.existsByBuyer_IdAndItem_Id(userId, item.getId()))
      return item;

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
  public Page<Item> getOwnItems(Pageable pageable, String text, UUID category, List<ItemState> states) {
    MyUserDetails user = authService.getRequiredAuthenticatedUserDetails();
    text = SqlUtils.prepareLikePattern(text);
    if (states == null || states.size() == 0)
      states = List.of(ItemState.AVAILABLE, ItemState.PENDING);
    Set<UUID> categoryIds = null;
    if (category != null) {
      categoryIds = categoryService.getCategoryDescendantsCache().get(category);
    }
    return itemRepository.findOwnItems(user.getId(), text, categoryIds, states, pageable);
  }

  @Override
  public void deleteItem(UUID id) {
    MyUserDetails user = authService.getRequiredAuthenticatedUserDetails();
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
  public Item publishItem(UUID id) {
    Item item = itemRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Item not found"));

    MyUserDetails user = authService.getRequiredAuthenticatedUserDetails();

    if (!Objects.equals(item.getOwner().getId(), user.getId())) {
      throw new ForbiddenException("Must be the owner of the item to update it");
    }
    if (!Objects.equals(item.getState(), ItemState.PENDING)) {
      throw new ForbiddenException("Unable to publish a non pending item");
    }

    item.setState(ItemState.AVAILABLE);

    return itemRepository.save(item);
  }

  @Override
  public Item hideItem(UUID id) {
    Item item = itemRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Item not found"));

    MyUserDetails user = authService.getRequiredAuthenticatedUserDetails();

    if (!Objects.equals(item.getOwner().getId(), user.getId())) {
      throw new ForbiddenException("Must be the owner of the item to update it");
    }
    if (!Objects.equals(item.getState(), ItemState.AVAILABLE)) {
      throw new ForbiddenException("Unable to hide a non available item");
    }

    item.setState(ItemState.PENDING);

    return itemRepository.save(item);
  }

  @Override
  public void deleteItemCompletely(Item item) {
    imageService.deleteImagesFromDisk(item.getImages());
    imageRepository.deleteAllByItemId(item.getId());
    itemRepository.delete(item);
  }

  @Override
  public void deleteItemsByUser(UUID userId) {
    List<Item> items = itemRepository.findByOwner_id(userId);
    for (Item item : items) {
      imageService.deleteImagesFromDisk(item.getImages());

      imageRepository.deleteAllByItemId(item.getId());
    }
    itemRepository.deleteAll(items);

  }

  @Override
  public void deleteItemsByUserIds(List<UUID> userIds) {
    List<Item> items = itemRepository.findByOwnerIdIn(userIds);
    for (Item item : items) {
      imageService.deleteImagesFromDisk(item.getImages());

      imageRepository.deleteAllByItemId(item.getId());
    }

    itemRepository.deleteAll(items);

  }

  @Override
  public int getUserItemCount(UUID userId) {
    return itemRepository.countByOwner_Id(userId);
  }

  @Override
  public List<Item> getUserItems(UUID userId) {

    return itemRepository.findByOwner_IdAndState(userId, ItemState.AVAILABLE);
  }

}
