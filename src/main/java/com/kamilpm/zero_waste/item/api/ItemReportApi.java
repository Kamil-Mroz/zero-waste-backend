package com.kamilpm.zero_waste.item.api;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import com.kamilpm.zero_waste.common.entity.ModerationStatus;
import com.kamilpm.zero_waste.common.exception.EntityNotFoundException;
import com.kamilpm.zero_waste.common.exception.ForbiddenException;
import com.kamilpm.zero_waste.item.entity.Item;
import com.kamilpm.zero_waste.item.repository.ItemRepository;
import com.kamilpm.zero_waste.item.service.ItemService;
import com.kamilpm.zero_waste.user.api.UserItemApi;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ItemReportApi {
  private final ItemRepository itemRepository;
  private final UserItemApi userItemApi;
  private final ApplicationEventPublisher events;
  private final ItemService itemService;

  public void itemExists(UUID subjectId, UUID userId) {
    Item item = itemRepository.findById(subjectId).orElseThrow(() -> new EntityNotFoundException("Item not found"));

    if (Objects.equals(item.getOwnerId(), userId))
      throw new ForbiddenException("You can not report yourself");

    if (userItemApi.isUserDemo(item.getOwnerId()))
      throw new ForbiddenException("Unable to interact with demo users");

    if (Objects.equals(item.getModerationStatus(), ModerationStatus.HIDDEN))
      throw new ForbiddenException("Unable to report a hidden item");

    if (!Objects.equals(item.getState(), ItemState.AVAILABLE))
      throw new ForbiddenException("Only available items can be reported");

  }

  public boolean isItemOwner(UUID itemId, UUID userId) {
    return itemRepository.existsByIdAndOwnerId(itemId, userId);
  }

  @Transactional
  public void deleteItemById(UUID itemId) {

    itemService.deleteItemCompletely(itemRepository.findById(itemId).orElse(null));
    events.publishEvent(new DeleteItemEvent(itemId));

  }

  public void hideItem(UUID adminId, UUID subjectId) {
    Item item = itemRepository.findByIdAndModerationStatus(subjectId, ModerationStatus.VISIBLE)
        .orElseThrow(() -> new EntityNotFoundException("Item not found"));
    item.setModeratedAt(Instant.now());
    item.setModeratedBy(adminId);
    item.setModerationStatus(ModerationStatus.HIDDEN);
    itemRepository.save(item);
  }

}
