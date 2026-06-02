package com.kamilpm.zero_waste.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.kamilpm.zero_waste.domain.entity.Offer;
import com.kamilpm.zero_waste.domain.entity.OfferStatus;

public interface OfferRepository extends JpaRepository<Offer, UUID> {

  @EntityGraph(attributePaths = { "buyer" })
  List<Offer> findByItem_idAndStatusAndIdNot(UUID itemId, OfferStatus status, UUID id);

  boolean existsByBuyer_IdAndItem_Id(UUID buyerId, UUID itemId);

  @EntityGraph(attributePaths = { "buyer" })
  Optional<Offer> findByBuyer_IdAndItem_Id(UUID buyerId, UUID itemId);

  @EntityGraph(attributePaths = { "buyer", "item", "item.owner" })
  Optional<Offer> findDetailsById(UUID id);

  @EntityGraph(attributePaths = { "buyer", "item", "item.owner" })
  Page<Offer> findByItem_Owner_IdAndStatus(UUID ownerId, OfferStatus status, Pageable pageable);

  @EntityGraph(attributePaths = { "buyer", "item", "item.owner" })
  Page<Offer> findByBuyer_IdAndStatus(UUID buyerId, OfferStatus status, Pageable pageable);

  @EntityGraph(attributePaths = { "buyer", "item", "item.owner" })
  Page<Offer> findByItem_Owner_Id(UUID ownerId, Pageable pageable);

  @EntityGraph(attributePaths = { "buyer", "item", "item.owner" })
  Page<Offer> findByBuyer_Id(UUID buyerId, Pageable pageable);

}
