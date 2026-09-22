package com.kamilpm.zero_waste.item.service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.kamilpm.zero_waste.common.dto.CategoryData;
import com.kamilpm.zero_waste.common.dto.CurrentUser;
import com.kamilpm.zero_waste.common.dto.ImageData;
import com.kamilpm.zero_waste.common.dto.ItemState;
import com.kamilpm.zero_waste.common.dto.UserRole;
import com.kamilpm.zero_waste.common.dto.UserSummaryDto;
import com.kamilpm.zero_waste.common.dto.UserVisibility;
import com.kamilpm.zero_waste.common.entity.ModerationStatus;
import com.kamilpm.zero_waste.common.events.BanEvent;
import com.kamilpm.zero_waste.common.events.DeleteImagesEvent;
import com.kamilpm.zero_waste.common.events.OfferAcceptEvent;
import com.kamilpm.zero_waste.common.events.RejectReportEvent;
import com.kamilpm.zero_waste.common.events.UnbanEvent;
import com.kamilpm.zero_waste.common.exception.ConflictException;
import com.kamilpm.zero_waste.common.exception.EntityNotFoundException;
import com.kamilpm.zero_waste.common.exception.ForbiddenException;
import com.kamilpm.zero_waste.common.interfaces.CategoryProvider;
import com.kamilpm.zero_waste.common.interfaces.CurrentUserProvider;
import com.kamilpm.zero_waste.common.interfaces.ImageProvider;
import com.kamilpm.zero_waste.common.interfaces.OfferProvider;
import com.kamilpm.zero_waste.common.interfaces.UserProvider;
import com.kamilpm.zero_waste.common.utils.SqlUtils;
import com.kamilpm.zero_waste.item.dto.ItemDto;
import com.kamilpm.zero_waste.item.dto.ItemListDto;
import com.kamilpm.zero_waste.item.dto.ItemRequest;
import com.kamilpm.zero_waste.item.dto.UpdateItemRequest;
import com.kamilpm.zero_waste.item.entity.Item;
import com.kamilpm.zero_waste.item.mapper.ItemMapper;
import com.kamilpm.zero_waste.item.repository.ItemRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ItemService {

  private final ItemRepository itemRepository;
  private final ItemMapper itemMapper;
  private final UserProvider userApi;
  private final CurrentUserProvider currentUser;
  private final CategoryProvider categoryItemApi;
  private final ImageProvider imageItemApi;
  private final OfferProvider offerProvider;
  private final ApplicationEventPublisher events;

  @Transactional
  public ItemDto createItem(ItemRequest itemRequest) {
    CurrentUser user = currentUser.getRequiredAuthenticatedUser();

    List<MultipartFile> files = itemRequest.getImages() == null ? List.of() : itemRequest.getImages();

    if (files.size() > 5) {
      throw new ConflictException("Max image count is 5", "images");
    }

    if (Objects.equals(itemRequest.getState(), ItemState.GIVEN)) {
      throw new ConflictException("Unable to create a given item");
    }

    CategoryData category = categoryItemApi.getCategoryById(itemRequest.getCategoryId());

    Item item = Item.builder()
        .title(itemRequest.getTitle())
        .description(itemRequest.getDescription())
        .condition(itemRequest.getCondition())
        .state(itemRequest.getState())
        .city(itemRequest.getCity())
        .ownerVisibility(UserVisibility.VISIBLE)
        .categoryId(itemRequest.getCategoryId())
        .ownerId(user.id())
        .build();

    Item savedItem = itemRepository.save(item);

    List<ImageData> uploadedImages = imageItemApi.uploadItemImages(savedItem.getId(), files);
    savedItem.setImageIds(new ArrayList<>(uploadedImages.stream().map((image) -> image.id()).toList()));

    ImageData thumbnail = null;

    if (!uploadedImages.isEmpty()) {
      Integer thumbnailIndex = itemRequest.getThumbnailIndex();

      thumbnail = thumbnailIndex != null && thumbnailIndex >= 0 && thumbnailIndex < uploadedImages.size()
          ? uploadedImages.get(thumbnailIndex)
          : uploadedImages.get(0);

      savedItem.setThumbnailId(thumbnail.id());
    }

    Item finalItem = itemRepository.save(savedItem);
    return itemMapper.toDto(finalItem, category, uploadedImages, thumbnail);
  }

  @Transactional
  public ItemDto updateItem(UUID id, UpdateItemRequest itemRequest) {

    if (Objects.equals(itemRequest.getState(), ItemState.GIVEN)) {
      throw new ConflictException("Unable to update to a given item");
    }

    CategoryData category = categoryItemApi.getCategoryById(itemRequest.getCategoryId());

    Item item = itemRepository.findByIdAndModerationStatus(id, ModerationStatus.VISIBLE)
        .orElseThrow(() -> new EntityNotFoundException("Item not found"));

    CurrentUser user = currentUser.getRequiredAuthenticatedUser();

    if (!Objects.equals(item.getOwnerId(), user.id())) {
      throw new ForbiddenException("Must be the owner of the item to update it");
    }

    if (Objects.equals(ItemState.GIVEN, item.getState())) {
      throw new ForbiddenException("Can not update a given item");
    }
    Set<UUID> removedImageIds = itemRequest.getRemovedImageIds() == null
        ? Set.of()
        : itemRequest.getRemovedImageIds();

    List<MultipartFile> newImages = itemRequest.getImages() == null ? List.of() : itemRequest.getImages();

    List<UUID> existingImageIds = new ArrayList<>(item.getImageIds());

    Set<UUID> existingImageIdSet = new HashSet<>(existingImageIds);

    if (!existingImageIdSet.containsAll(removedImageIds)) {
      throw new ForbiddenException("Some images do not belong to this item");
    }

    int finalImageCount = existingImageIds.size() - removedImageIds.size() + newImages.size();

    if (finalImageCount > 5) {
      throw new ConflictException("Max image count is 5", "images");
    }

    item.setTitle(itemRequest.getTitle());
    item.setDescription(itemRequest.getDescription());
    item.setCondition(itemRequest.getCondition());
    item.setCity(itemRequest.getCity());
    item.setCategoryId(itemRequest.getCategoryId());
    item.setState(itemRequest.getState());

    List<UUID> remainingImageIds = existingImageIds.stream().filter(imageId -> !removedImageIds.contains(imageId))
        .collect(Collectors.toCollection(ArrayList::new));

    if (item.getThumbnailId() != null && removedImageIds.contains(item.getThumbnailId())) {
      item.setThumbnailId(null);
    }

    List<ImageData> remainingImages = new ArrayList<>(imageItemApi.getAllImagesByIds(remainingImageIds));
    List<ImageData> uploadedImages = imageItemApi.uploadItemImages(item.getId(), newImages);

    remainingImages.addAll(uploadedImages);

    remainingImageIds.addAll(uploadedImages.stream().map(image -> image.id()).toList());
    item.setImageIds(remainingImageIds);

    ImageData thumbnail = updateThumbnail(item, itemRequest, remainingImages);
    Item updatedItem = itemRepository.save(item);

    if (!removedImageIds.isEmpty()) {
      events.publishEvent(new DeleteImagesEvent(removedImageIds));
    }

    return itemMapper.toDto(updatedItem, category, remainingImages, thumbnail);
  }

  private ImageData updateThumbnail(Item item, UpdateItemRequest request,
      List<ImageData> images) {
    if (images.isEmpty()) {
      item.setThumbnailId(null);
      return null;

    }
    Integer thumbnailIndex = request.getThumbnailIndex();

    if (thumbnailIndex != null
        && thumbnailIndex >= 0
        && thumbnailIndex < images.size()) {
      images.get(thumbnailIndex);
    }
    if (item.getThumbnailId() != null
        && images.stream().anyMatch((image) -> Objects.equals(image.id(), item.getThumbnailId()))) {
      for (ImageData image : images) {
        if (Objects.equals(image.id(), item.getThumbnailId())) {
          return image;
        }
      }
    }

    ImageData image = images.get(0);
    item.setThumbnailId(image.id());
    return image;

  }

  @Transactional(readOnly = true)
  public Page<ItemListDto> getItems(Pageable pageable, String text, UUID categoryId) {
    text = SqlUtils.prepareLikePattern(text);
    Set<UUID> categoryIds = null;
    if (categoryId != null) {
      categoryIds = categoryItemApi.getCategoryDescendantsById(categoryId);
    }
    Optional<CurrentUser> user = currentUser.getAuthenticatedUser();
    UUID excludeOwnerId = user.map(owner -> owner.id()).orElse(null);

    Page<Item> itemPage = itemRepository.searchItems(excludeOwnerId, ItemState.AVAILABLE, text,
        ModerationStatus.VISIBLE, categoryIds, UserVisibility.VISIBLE, pageable);

    Set<UUID> itemsCategoryIds = itemPage.getContent().stream().map(item -> item.getCategoryId())
        .collect(Collectors.toSet());

    Map<UUID, CategoryData> categoriesById = categoryItemApi.getCategoriesByIds(itemsCategoryIds);

    Set<UUID> itemsImageIds = itemPage.getContent().stream().map(item -> item.getThumbnailId())
        .collect(Collectors.toSet());
    Map<UUID, ImageData> imagesById = imageItemApi.getImagesByIds(itemsImageIds);

    return itemPage.map((item) -> itemMapper.toListDto(item, categoriesById.get(item.getCategoryId()),
        imagesById.get(item.getThumbnailId())));
  }

  @Transactional(readOnly = true)
  public ItemDto getItem(UUID id) {
    Item item = itemRepository.findById(id)
        .orElseThrow(() -> new EntityNotFoundException("Item not found"));

    CategoryData category = categoryItemApi.getCategoryById(item.getCategoryId());
    List<ImageData> images = imageItemApi.getImagesByItemId(item.getId());
    ImageData thumbnail = null;
    if (images != null && images.size() > 0 && item.getThumbnailId() != null)
      thumbnail = images.stream().filter(image -> Objects.equals(image.id(), item.getThumbnailId())).findFirst()
          .orElse(null);

    UserSummaryDto owner = userApi.findUserSummaryById(item.getOwnerId());

    if (Objects.equals(item.getState(), ItemState.AVAILABLE)
        && Objects.equals(item.getModerationStatus(), ModerationStatus.VISIBLE)
        && !userApi.isUserDemo(item.getOwnerId()) && Objects.equals(item.getOwnerVisibility(), UserVisibility.VISIBLE))
      return itemMapper.toDtoWithOwner(item, category, images, thumbnail, owner);

    CurrentUser user = currentUser.getRequiredAuthenticatedUser();
    UUID userId = user.id();

    if (Objects.equals(user.role(), UserRole.ADMIN)) {
      return itemMapper.toDtoWithOwner(item, category, images, thumbnail, owner);
    }

    if (userId.equals(item.getOwnerId()))
      return itemMapper.toDtoWithOwner(item, category, images, thumbnail, owner);

    if (Objects.equals(item.getState(), ItemState.GIVEN) && offerProvider.isBuyerOfItem(userId, item.getId())
        && Objects.equals(item.getModerationStatus(), ModerationStatus.VISIBLE))
      return itemMapper.toDtoWithOwner(item, category, images, thumbnail, owner);

    throw new EntityNotFoundException("Item not available");

  }

  public Item findByIdForUpdate(UUID id) {
    return itemRepository.findByIdForUpdate(id).orElseThrow(() -> new EntityNotFoundException("Item not found"));
  }

  public void saveItem(Item item) {
    itemRepository.save(item);
  }

  @Transactional(readOnly = true)
  public Page<ItemListDto> getOwnItems(Pageable pageable, String text, UUID category, List<ItemState> states) {
    CurrentUser user = currentUser.getRequiredAuthenticatedUser();
    text = SqlUtils.prepareLikePattern(text);
    if (states == null || states.size() == 0)
      states = List.of(ItemState.AVAILABLE, ItemState.PENDING);
    Set<UUID> categoryIds = null;
    if (category != null) {
      categoryIds = categoryItemApi.getCategoryDescendantsById(category);
    }
    Page<Item> itemPage = itemRepository.findOwnItems(user.id(), text, categoryIds, states, pageable);
    Set<UUID> itemsCategoryIds = itemPage.getContent().stream().map(item -> item.getCategoryId())
        .collect(Collectors.toSet());
    Map<UUID, CategoryData> categoriesById = categoryItemApi.getCategoriesByIds(itemsCategoryIds);
    Set<UUID> itemsImageIds = itemPage.getContent().stream().map(item -> item.getThumbnailId())
        .collect(Collectors.toSet());

    Map<UUID, ImageData> imagesById = imageItemApi.getImagesByIds(itemsImageIds);

    return itemPage.map((item) -> itemMapper.toListDto(item, categoriesById.get(item.getCategoryId()),
        imagesById.get(item.getThumbnailId())));

  }

  @Transactional
  public void deleteItem(UUID id) {
    CurrentUser user = currentUser.getRequiredAuthenticatedUser();
    Item item = itemRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Item not found"));
    boolean isAdmin = user.role() == UserRole.ADMIN;

    if (!Objects.equals(user.id(), item.getOwnerId()) && !isAdmin) {
      throw new ForbiddenException("Others items can not be deleted");
    }
    if (Objects.equals(item.getState(), ItemState.GIVEN)) {
      throw new ForbiddenException("Given item can not be deleted");
    }

    deleteItemCompletely(item);

    events.publishEvent(new RejectReportEvent(item.getId(), isAdmin));

  }

  @Transactional
  public void publishItem(UUID id) {
    Item item = itemRepository.findByIdAndModerationStatus(id, ModerationStatus.VISIBLE)
        .orElseThrow(() -> new EntityNotFoundException("Item not found"));

    CurrentUser user = currentUser.getRequiredAuthenticatedUser();

    if (!Objects.equals(item.getOwnerId(), user.id())) {
      throw new ForbiddenException("Must be the owner of the item to update it");
    }
    if (!Objects.equals(item.getState(), ItemState.PENDING)) {
      throw new ForbiddenException("Unable to publish a non pending item");
    }

    item.setState(ItemState.AVAILABLE);

    itemRepository.save(item);
  }

  @Transactional
  public void hideItem(UUID id) {
    Item item = itemRepository.findByIdAndModerationStatus(id, ModerationStatus.VISIBLE)
        .orElseThrow(() -> new EntityNotFoundException("Item not found"));

    CurrentUser user = currentUser.getRequiredAuthenticatedUser();

    if (!Objects.equals(item.getOwnerId(), user.id())) {
      throw new ForbiddenException("Must be the owner of the item to update it");
    }
    if (!Objects.equals(item.getState(), ItemState.AVAILABLE)) {
      throw new ForbiddenException("Unable to hide a non available item");
    }

    item.setState(ItemState.PENDING);

    itemRepository.save(item);
  }

  @Transactional
  public void deleteItemCompletely(Item item) {
    if (item == null) {
      return;
    }

    imageItemApi.deleteImages(item.getImageIds());
    itemRepository.delete(item);
  }

  @Transactional(readOnly = true)
  public List<ItemListDto> getUserItems(UUID userId) {
    List<Item> items = itemRepository
        .findByOwnerIdAndStateAndModerationStatusAndOwnerVisibility(userId, ItemState.AVAILABLE,
            ModerationStatus.VISIBLE, UserVisibility.VISIBLE);
    Set<UUID> itemsCategoryIds = items.stream().map(item -> item.getCategoryId()).collect(Collectors.toSet());

    Map<UUID, CategoryData> categoriesById = categoryItemApi.getCategoriesByIds(itemsCategoryIds);

    Set<UUID> itemsImageIds = items.stream().map(item -> item.getThumbnailId()).collect(Collectors.toSet());

    Map<UUID, ImageData> imagesById = imageItemApi.getImagesByIds(
        itemsImageIds);

    return items.stream().map((item) -> itemMapper.toListDto(item, categoriesById.get(item.getCategoryId()),
        imagesById.get(item.getThumbnailId()))).toList();
  }

  public boolean existsByCategory_Id(UUID categoryId) {
    return itemRepository.existsByCategoryId(categoryId);
  }

  @ApplicationModuleListener
  void on(OfferAcceptEvent event) {
    itemRepository.updateItemState(event.itemId(), ItemState.GIVEN);
  }

  @ApplicationModuleListener
  void on(BanEvent event) {
    itemRepository.updateOwnerVisibility(event.ids(), UserVisibility.BANNED, ItemState.GIVEN);
  }

  @ApplicationModuleListener
  void on(UnbanEvent event) {
    itemRepository.updateOwnerVisibility(event.ids(), UserVisibility.VISIBLE, ItemState.GIVEN);
  }

}
