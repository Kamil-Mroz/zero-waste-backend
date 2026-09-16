package com.kamilpm.zero_waste.common.interfaces;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import com.kamilpm.zero_waste.common.dto.ProfileItemSummary;
import com.kamilpm.zero_waste.common.dto.SimpleItemData;

public interface ItemProvider {

  public SimpleItemData getItemById(UUID itemId);

  boolean existsByCategoryId(UUID categoryId);

  public SimpleItemData findByIdForUpdate(UUID itemId);

  public SimpleItemData findById(UUID itemId);

  public Map<UUID, SimpleItemData> getItemsByIds(Collection<UUID> ids);

  public Map<UUID, SimpleItemData> getItemsOwnedBy(UUID userId);

  public Set<UUID> findByUserIds(List<UUID> ids);

  public ProfileItemSummary buildItemSummary(UUID userId);

  public void itemExists(UUID subjectId, UUID userId);

  public boolean isItemOwner(UUID itemId, UUID userId);

  public void deleteItemById(UUID itemId);

  public void hideItem(UUID adminId, UUID subjectId);

  public void deleteItemsByOwnerIds(Collection<UUID> ids);
}
