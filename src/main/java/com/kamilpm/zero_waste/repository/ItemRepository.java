package com.kamilpm.zero_waste.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.kamilpm.zero_waste.domain.entity.Item;
import com.kamilpm.zero_waste.domain.entity.ItemState;

@Repository
public interface ItemRepository extends JpaRepository<Item, UUID> {
  @Query("SELECT DISTINCT i FROM Item i JOIN FETCH i.owner o JOIN FETCH i.category c LEFT JOIN FETCH i.images  WHERE o.id != ?1 and i.state != ?2")
  List<Item> findByOwner_IdNotAndStateNot(UUID ownerId, ItemState state);

  @Query("SELECT DISTINCT i FROM Item i JOIN FETCH i.owner o JOIN FETCH i.category c LEFT JOIN FETCH i.images WHERE o.id = ?1")
  List<Item> findByOwner_Id(UUID ownerId);

  @Query("SELECT DISTINCT i FROM Item i JOIN FETCH i.owner o JOIN FETCH i.category c  LEFT JOIN FETCH i.images WHERE i.state != ?1")
  List<Item> findByStateNot(ItemState state);

  boolean existsByCategory_Id(UUID id);

  @Query("SELECT DISTINCT i FROM Item i JOIN FETCH i.owner JOIN FETCH i.category LEFT JOIN FETCH i.images WHERE i.id = ?1")
  Optional<Item> findByIdWithOwnerAndCategoryAndImages(UUID id);

}
