package com.kamilpm.zero_waste.service.impl;

import java.util.UUID;

import org.springframework.stereotype.Service;

import com.kamilpm.zero_waste.repository.ItemRepository;
import com.kamilpm.zero_waste.repository.OfferRepository;
import com.kamilpm.zero_waste.service.ItemOwnershipService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ItemOwnershipServiceImpl implements ItemOwnershipService {
  private final OfferRepository offerRepository;
  private final ItemRepository itemRepository;

  @Override
  public boolean isBuyerOfItem(UUID itemId, UUID userId) {
    return offerRepository.existsByBuyer_IdAndItem_Id(userId, itemId);
  }

  @Override
  public boolean isOwnedBy(UUID itemId, UUID userId) {
    return itemRepository.existsByIdAndOwner_Id(itemId, userId);
  }

}
