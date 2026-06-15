package com.kamilpm.zero_waste.service;

import java.util.UUID;

public interface ItemOwnershipService {
  boolean isOwnedBy(UUID itemId, UUID userId);

  boolean isBuyerOfItem(UUID itemId, UUID userId);

}
