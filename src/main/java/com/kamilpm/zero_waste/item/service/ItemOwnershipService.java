package com.kamilpm.zero_waste.item.service;

import java.util.UUID;

import org.springframework.stereotype.Service;

// import com.kamilpm.zero_waste.repository.ItemRepository;
// import com.kamilpm.zero_waste.repository.OfferRepository;
// import com.kamilpm.zero_waste.service.ItemOwnershipService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ItemOwnershipService {
  // private final OfferRepository offerRepository;
  // private final ItemRepository itemRepository;

  // @Override
  // public boolean isBuyerOfItem(UUID userId, UUID itemId) {
  // return offerRepository.existsByBuyer_IdAndItem_Id(userId, itemId);
  // }

  // @Override
  // public boolean isOwnedBy(UUID itemId, UUID userId) {
  // return itemRepository.existsByIdAndOwner_Id(itemId, userId);
  // }

}
