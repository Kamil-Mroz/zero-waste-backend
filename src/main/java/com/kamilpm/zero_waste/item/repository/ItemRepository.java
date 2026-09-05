package com.kamilpm.zero_waste.item.repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.kamilpm.zero_waste.common.entity.ModerationStatus;
import com.kamilpm.zero_waste.item.api.ItemState;
import com.kamilpm.zero_waste.item.entity.Item;
import com.kamilpm.zero_waste.item.interfaces.IItemCount;

import jakarta.persistence.LockModeType;

@Repository
public interface ItemRepository extends JpaRepository<Item, UUID> {
  @Query("""
      SELECT DISTINCT i
      FROM Item i
      WHERE i.state = :state
      AND (:ownerId IS NULL OR i.ownerId != :ownerId)
      AND i.moderationStatus = :moderationStatus
      AND (:categoryIds IS NULL OR i.categoryId IN :categoryIds)
      AND (:text IS NULL
        OR LOWER(i.title) LIKE :text ESCAPE '\\'
        OR LOWER(i.city) LIKE :text ESCAPE '\\'
      )
        order by i.createdAt desc
        """)
  Page<Item> searchItems(@Param("ownerId") UUID ownerId, @Param("state") ItemState state, @Param("text") String text,
      @Param("moderationStatus") ModerationStatus moderationStatus, @Param("categoryIds") Set<UUID> categoryIds,
      Pageable pageable);

  @Query("""
        SELECT DISTINCT i
        FROM Item i
        WHERE i.ownerId = :ownerId
        AND (:categoryIds IS NULL OR i.categoryId IN :categoryIds)
        AND (:text IS NULL
        OR LOWER(i.title) LIKE :text ESCAPE '\\'
        OR LOWER(i.city) LIKE :text ESCAPE '\\'
        )
        AND i.state IN :states
        order by i.createdAt desc
      """)
  Page<Item> findOwnItems(@Param("ownerId") UUID ownerId, @Param("text") String text,
      @Param("categoryIds") Set<UUID> categoryIds, @Param("states") List<ItemState> states, Pageable pageable);

  boolean existsByCategoryId(UUID id);

  @Query("SELECT DISTINCT i FROM Item i WHERE i.id = ?1")
  Optional<Item> findByIdWithOwnerAndCategoryAndImages(UUID id);

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("SELECT i FROM Item i WHERE i.id = :id")
  Optional<Item> findByIdForUpdate(@Param("id") UUID id);

  int countByOwnerId(UUID userId);

  @Query("select i.state as itemState, COUNT(i.state) as totalItem from Item as i where i.ownerId = :userId group by i.state")
  List<IItemCount> countTotalItemsByOwnerIdAndState(@Param("userId") UUID userId);

  List<Item> findTop3ByOwnerIdAndStateAndModerationStatusOrderByCreatedAtDesc(UUID ownerId, ItemState itemState,
      ModerationStatus status);

  List<Item> findByOwnerIdAndStateAndModerationStatus(UUID id, ItemState state, ModerationStatus moderationStatus);

  List<Item> findByOwnerIdAndModerationStatus(UUID ownerId, ModerationStatus moderationStatus);

  List<Item> findByOwnerIdIn(List<UUID> userIds);

  List<Item> findByOwnerId(UUID userId);

  boolean existsByIdAndOwnerId(UUID itemId, UUID userId);

  Optional<Item> findById(UUID id);

  Optional<Item> findByIdAndModerationStatus(UUID id, ModerationStatus status);

  boolean existsByIdAndOwnerIdNotAndState(UUID id, UUID userId, ItemState state);

  @Modifying
  @Query("Update Item i set i.state = :itemState WHERE i.id = :itemId")
  void updateItemState(@Param("itemId") UUID itemId, @Param("itemState") ItemState itemState);
}
