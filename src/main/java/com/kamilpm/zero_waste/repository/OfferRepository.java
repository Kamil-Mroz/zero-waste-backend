package com.kamilpm.zero_waste.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.kamilpm.zero_waste.domain.entity.Offer;

public interface OfferRepository extends JpaRepository<Offer, UUID> {

  @EntityGraph(attributePaths = { "buyer" })
  List<Offer> findByItem_idAndIdNot(UUID itemId, UUID id);

}
