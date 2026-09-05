package com.kamilpm.zero_waste.offer.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.kamilpm.zero_waste.offer.entity.Offer;
import com.kamilpm.zero_waste.offer.entity.OfferStatus;

public interface OfferRepository extends JpaRepository<Offer, UUID> {

  List<Offer> findByItemIdAndStatusAndIdNot(UUID itemId, OfferStatus status, UUID id);

  List<Offer> findByItemId(UUID itemId);

  Optional<Offer> findByItemIdAndStatus(UUID itemId, OfferStatus status);

  boolean existsByBuyerIdAndItemId(UUID buyerId, UUID itemId);

  Optional<Offer> findByBuyerIdAndItemId(UUID buyerId, UUID itemId);

  Optional<Offer> findDetailsById(UUID id);

  // Page<Offer> findByItem_Owner_IdAndStatus(UUID ownerId, OfferStatus status,
  // Pageable pageable);

  Page<Offer> findByBuyerIdAndStatus(UUID buyerId, OfferStatus status, Pageable pageable);

  Page<Offer> findByItemIdInAndStatus(Set<UUID> itemIds, OfferStatus status, Pageable pageable);

  Page<Offer> findByItemIdIn(Collection<UUID> itemIds, Pageable pageable);

  List<Offer> findByItemIdIn(Collection<UUID> itemIds);

  // Page<Offer> findByItem_Owner_Id(UUID ownerId, Pageable pageable);

  Page<Offer> findByBuyerId(UUID buyerId, Pageable pageable);

  // void deleteByItem_Owner_IdIn(List<UUID> ids);
  void deleteByItemIdIn(Collection<UUID> ids);

  void deleteByBuyerIdIn(List<UUID> ids);

  void deleteAllByItemId(UUID id);

  void deleteAllByItemIdIn(Collection<UUID> id);
}
