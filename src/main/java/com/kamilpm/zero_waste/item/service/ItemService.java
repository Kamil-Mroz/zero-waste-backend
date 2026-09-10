package com.kamilpm.zero_waste.item.service;

import com.kamilpm.zero_waste.user.api.UserItemApi;

import java.util.ArrayList;
import java.util.Collection;
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

import com.kamilpm.zero_waste.auth.api.CurrentUserApi;
import com.kamilpm.zero_waste.category.api.CategoryItemApi;
import com.kamilpm.zero_waste.item.dto.CategoryDto;
import com.kamilpm.zero_waste.item.dto.ImageDto;
import com.kamilpm.zero_waste.common.entity.ModerationStatus;
import com.kamilpm.zero_waste.common.exception.ConflictException;
import com.kamilpm.zero_waste.common.exception.EntityNotFoundException;
import com.kamilpm.zero_waste.common.exception.ForbiddenException;
import com.kamilpm.zero_waste.common.utils.OwnMapper;
import com.kamilpm.zero_waste.common.utils.SqlUtils;
import com.kamilpm.zero_waste.image.api.ImageItemApi;
import com.kamilpm.zero_waste.item.api.DeleteItemEvent;
import com.kamilpm.zero_waste.item.api.DeleteItemsEvent;
import com.kamilpm.zero_waste.item.dto.AuthenticatedUser;
import com.kamilpm.zero_waste.item.dto.ItemDto;
import com.kamilpm.zero_waste.item.dto.ItemListDto;
import com.kamilpm.zero_waste.item.dto.ItemRequest;
import com.kamilpm.zero_waste.item.dto.ItemState;
import com.kamilpm.zero_waste.item.dto.UpdateItemRequest;
import com.kamilpm.zero_waste.item.dto.UserRole;
import com.kamilpm.zero_waste.item.dto.UserSummaryDto;
import com.kamilpm.zero_waste.item.entity.Item;
import com.kamilpm.zero_waste.item.mapper.ItemMapper;
import com.kamilpm.zero_waste.item.repository.ItemRepository;
import com.kamilpm.zero_waste.moderation.api.RejectReportEvent;
import com.kamilpm.zero_waste.offer.api.OfferAcceptEvent;
import com.kamilpm.zero_waste.offer.api.OfferItemApi;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ItemService {

  private final UserItemApi userItemApi;
  private final CategoryItemApi categoryItemApi;
  private final ItemRepository itemRepository;
  private final CurrentUserApi currentUser;
  private final ImageItemApi imageItemApi;
  private final OfferItemApi offerItemApi;
  private final ItemMapper itemMapper;
  // private final OfferItemQuery offerService;
  // private final ReportService reportService;
  private final ApplicationEventPublisher events;

  @Transactional
  public ItemDto createItem(ItemRequest itemRequest) {
    AuthenticatedUser user = getRequiredAuthenticatedUser();

    List<MultipartFile> files = itemRequest.getImages() == null ? List.of() : itemRequest.getImages();

    if (files.size() > 5) {
      throw new ConflictException("Max image count is 5", "images");
    }

    if (Objects.equals(itemRequest.getState(), ItemState.GIVEN)) {
      throw new ConflictException("Unable to create a given item");
    }

    CategoryDto category = getCategoryById(itemRequest.getCategoryId());

    Item item = Item.builder()
        .title(itemRequest.getTitle())
        .description(itemRequest.getDescription())
        .condition(itemRequest.getCondition())
        .state(itemRequest.getState())
        .city(itemRequest.getCity())
        .categoryId(itemRequest.getCategoryId())
        .ownerId(user.id())
        .build();

    Item savedItem = itemRepository.save(item);

    List<ImageDto> uploadedImages = uploadItemImages(savedItem.getId(), files);
    savedItem.setImageIds(new ArrayList<>(uploadedImages.stream().map((image) -> image.id()).toList()));

    ImageDto thumbnail = null;

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

    CategoryDto category = getCategoryById(itemRequest.getCategoryId());

    Item item = itemRepository.findByIdAndModerationStatus(id, ModerationStatus.VISIBLE)
        .orElseThrow(() -> new EntityNotFoundException("Item not found"));

    AuthenticatedUser user = getRequiredAuthenticatedUser();

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

    if (!removedImageIds.isEmpty()) {
      imageItemApi.deleteItemImages(item.getId(), new ArrayList<>(removedImageIds));
    }

    List<ImageDto> remainingImages = new ArrayList<>(getAllImagesByIds(remainingImageIds));
    List<ImageDto> uploadedImages = uploadItemImages(item.getId(), newImages);

    remainingImages.addAll(uploadedImages);

    remainingImageIds.addAll(uploadedImages.stream().map(image -> image.id()).toList());
    item.setImageIds(remainingImageIds);

    ImageDto thumbnail = updateThumbnail(item, itemRequest, remainingImages);
    Item updatedItem = itemRepository.save(item);
    return itemMapper.toDto(updatedItem, category, remainingImages, thumbnail);
  }

  private ImageDto updateThumbnail(Item item, UpdateItemRequest request,
      List<ImageDto> images) {
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
      for (ImageDto image : images) {
        if (Objects.equals(image.id(), item.getThumbnailId())) {
          return image;
        }
      }
    }

    ImageDto image = images.get(0);
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
    Optional<AuthenticatedUser> user = getAuthenticatedUser();
    UUID excludeOwnerId = user.map(owner -> owner.id()).orElse(null);

    Page<Item> itemPage = itemRepository.searchItems(excludeOwnerId, ItemState.AVAILABLE, text,
        ModerationStatus.VISIBLE, categoryIds, pageable);

    Set<UUID> itemsCategoryIds = itemPage.getContent().stream().map(item -> item.getCategoryId())
        .collect(Collectors.toSet());

    Map<UUID, CategoryDto> categoriesById = getCategoriesByIds(itemsCategoryIds);

    Set<UUID> itemsImageIds = itemPage.getContent().stream().map(item -> item.getThumbnailId())
        .collect(Collectors.toSet());
    Map<UUID, ImageDto> imagesById = getImagesByIds(itemsImageIds);

    return itemPage.map((item) -> itemMapper.toListDto(item, categoriesById.get(item.getCategoryId()),
        imagesById.get(item.getThumbnailId())));
  }

  @Transactional(readOnly = true)
  public ItemDto getItem(UUID id) {
    Item item = itemRepository.findById(id)
        .orElseThrow(() -> new EntityNotFoundException("Item not found"));

    CategoryDto category = getCategoryById(item.getCategoryId());
    List<ImageDto> images = getImagesByItemId(item.getId());
    ImageDto thumbnail = null;
    if (images != null && images.size() > 0 && item.getThumbnailId() != null)
      thumbnail = images.stream().filter(image -> Objects.equals(image.id(), item.getThumbnailId())).findFirst()
          .orElse(null);

    UserSummaryDto owner = findByItemOwnerId(item.getOwnerId());

    if (Objects.equals(item.getState(), ItemState.AVAILABLE)
        && Objects.equals(item.getModerationStatus(), ModerationStatus.VISIBLE)
        && !userItemApi.isUserDemo(item.getOwnerId()))
      return itemMapper.toDtoWithOwner(item, category, images, thumbnail, owner);

    AuthenticatedUser user = getRequiredAuthenticatedUser();
    UUID userId = user.id();

    if (Objects.equals(user.role(), UserRole.ADMIN)) {
      return itemMapper.toDtoWithOwner(item, category, images, thumbnail, owner);
    }

    if (userId.equals(item.getOwnerId()))
      return itemMapper.toDtoWithOwner(item, category, images, thumbnail, owner);

    if (Objects.equals(item.getState(), ItemState.GIVEN) && offerItemApi.isBuyerOfItem(userId, item.getId())
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
    AuthenticatedUser user = getRequiredAuthenticatedUser();
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
    Map<UUID, CategoryDto> categoriesById = getCategoriesByIds(itemsCategoryIds);
    Set<UUID> itemsImageIds = itemPage.getContent().stream().map(item -> item.getThumbnailId())
        .collect(Collectors.toSet());

    Map<UUID, ImageDto> imagesById = getImagesByIds(itemsImageIds);

    return itemPage.map((item) -> itemMapper.toListDto(item, categoriesById.get(item.getCategoryId()),
        imagesById.get(item.getThumbnailId())));

  }

  @Transactional
  public void deleteItem(UUID id) {
    AuthenticatedUser user = getRequiredAuthenticatedUser();
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

    AuthenticatedUser user = getRequiredAuthenticatedUser();

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

    AuthenticatedUser user = getRequiredAuthenticatedUser();

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
    UUID itemId = item.getId();
    imageItemApi.deleteItemImages(itemId, item.getImageIds());
    itemRepository.delete(item);

    events.publishEvent(new DeleteItemEvent(itemId));

  }

  @Transactional
  public void deleteItemsByUserIds(List<UUID> userIds) {
    List<Item> items = itemRepository.findByOwnerIdIn(userIds);
    Set<UUID> itemIds = items.stream().map(item -> item.getId()).collect(Collectors.toSet());
    imageItemApi.deleteImagesByItems(itemIds);
    itemRepository.deleteAll(items);
    events.publishEvent(new DeleteItemsEvent(itemIds));
  }

  @Transactional(readOnly = true)
  public int getUserItemCount(UUID userId) {
    return itemRepository.countByOwnerId(userId);
  }

  @Transactional(readOnly = true)
  public List<ItemListDto> getUserItems(UUID userId) {

    List<Item> items = itemRepository
        .findByOwnerIdAndStateAndModerationStatus(userId, ItemState.AVAILABLE, ModerationStatus.VISIBLE);
    Set<UUID> itemsCategoryIds = items.stream().map(item -> item.getCategoryId()).collect(Collectors.toSet());

    Map<UUID, CategoryDto> categoriesById = getCategoriesByIds(itemsCategoryIds);

    Set<UUID> itemsImageIds = items.stream().map(item -> item.getThumbnailId()).collect(Collectors.toSet());

    Map<UUID, ImageDto> imagesById = getImagesByIds(
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

  private AuthenticatedUser getRequiredAuthenticatedUser() {
    return OwnMapper.map(currentUser.getRequiredAuthenticatedUser(), (user) -> new AuthenticatedUser(
        user.id(),
        user.email(),
        user.nickname(),
        user.password(),
        UserRole.valueOf(user.role().name()),
        user.banActive(),
        user.bannedUntil(),
        user.joinedAt()));
  }

  private Optional<AuthenticatedUser> getAuthenticatedUser() {
    return OwnMapper.mapOptional(currentUser.getAuthenticatedUser(), (user) -> new AuthenticatedUser(
        user.id(),
        user.email(),
        user.nickname(),
        user.password(),
        UserRole.valueOf(user.role().name()),
        user.banActive(),
        user.bannedUntil(),
        user.joinedAt()));
  }

  private UserSummaryDto findByItemOwnerId(UUID id) {
    return OwnMapper.map(userItemApi.findByItemOwnerId(id),
        (user) -> new UserSummaryDto(user.id(), user.nickname()));
  }

  private CategoryDto getCategoryById(UUID id) {
    return OwnMapper.map(categoryItemApi.getCategoryById(id),
        (category) -> new CategoryDto(category.getId(), category.getName(), category.getParentId()));
  }

  private List<ImageDto> getAllImagesByIds(List<UUID> ids) {
    return OwnMapper.mapList(imageItemApi.getAllImagesByIds(ids),
        image -> new ImageDto(image.id(), image.originalName(), image.url()));

  }

  private Map<UUID, CategoryDto> getCategoriesByIds(Collection<UUID> ids) {
    return OwnMapper.mapValues(categoryItemApi.getCategoriesByIds(ids),
        category -> new CategoryDto(category.getId(), category.getName(), category.getParentId()));
  }

  private Map<UUID, ImageDto> getImagesByIds(Collection<UUID> ids) {
    return OwnMapper.mapValues(imageItemApi.getImagesByIds(ids),
        img -> new ImageDto(img.id(), img.originalName(), img.url()));
  }

  private List<ImageDto> uploadItemImages(UUID itemId, List<MultipartFile> files) {
    return OwnMapper.mapList(imageItemApi.uploadItemImages(itemId, files),
        img -> new ImageDto(img.id(), img.originalName(), img.url()));
  }

  private List<ImageDto> getImagesByItemId(UUID id) {
    return OwnMapper.mapList(imageItemApi.getImagesByItemId(id),
        img -> new ImageDto(img.id(), img.originalName(), img.url()));
  }
}
