package com.kamilpm.zero_waste.offer.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.kamilpm.zero_waste.common.dto.OfferStatus;
import com.kamilpm.zero_waste.common.dto.UserVisibility;
import com.kamilpm.zero_waste.offer.entity.Offer;

public interface OfferRepository extends JpaRepository<Offer, UUID> {

  List<Offer> findByItemIdAndStatusAndIdNot(UUID itemId, OfferStatus status, UUID id);

  List<Offer> findByItemId(UUID itemId);

  Optional<Offer> findByItemIdAndStatus(UUID itemId, OfferStatus status);

  boolean existsByBuyerIdAndItemId(UUID buyerId, UUID itemId);

  Optional<Offer> findByBuyerIdAndItemId(UUID buyerId, UUID itemId);

  Optional<Offer> findDetailsById(UUID id);

  Page<Offer> findByBuyerIdAndStatus(UUID buyerId, OfferStatus status, Pageable pageable);

  Page<Offer> findByItemIdInAndStatusAndBuyerVisibility(Set<UUID> itemIds, OfferStatus status,
      UserVisibility visibility, Pageable pageable);

  Page<Offer> findByItemIdInAndBuyerVisibility(Collection<UUID> itemIds, UserVisibility visibility, Pageable pageable);

  List<Offer> findByItemIdIn(Collection<UUID> itemIds);

  Page<Offer> findByBuyerId(UUID buyerId, Pageable pageable);

  void deleteByItemIdIn(Collection<UUID> ids);

  void deleteByBuyerIdIn(List<UUID> ids);

  void deleteAllByItemId(UUID id);

  void deleteAllByItemIdIn(Collection<UUID> id);

  @Modifying(clearAutomatically = true)
  @Query("Update Offer o set o.buyerVisibility = :visibility WHERE o.buyerId IN :ownerIds AND o.status = :status")
  void updateBuyerVisibility(@Param("ownerIds") List<UUID> ownerIds, @Param("visibility") UserVisibility visibility,
      @Param("status") OfferStatus status);

  @Modifying(clearAutomatically = true)
  @Query("Update Offer o set o.buyerVisibility = :visibility WHERE o.buyerId = :ownerId AND o.status = :status")
  void updateBuyerVisibility(@Param("ownerId") UUID ownerId, @Param("visibility") UserVisibility visibility,
      @Param("status") OfferStatus status);
}
