package com.kamilpm.zero_waste.item.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.NativeQuery;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.kamilpm.zero_waste.common.dto.ItemState;
import com.kamilpm.zero_waste.common.dto.UserVisibility;
import com.kamilpm.zero_waste.common.entity.ModerationStatus;
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
      AND i.ownerVisibility = :visibility
      AND (:text IS NULL
        OR LOWER(i.title) LIKE :text ESCAPE '\\'
        OR LOWER(i.city) LIKE :text ESCAPE '\\'
      )
        order by i.createdAt desc
        """)
  Page<Item> searchItems(@Param("ownerId") UUID ownerId, @Param("state") ItemState state, @Param("text") String text,
      @Param("moderationStatus") ModerationStatus moderationStatus, @Param("categoryIds") Set<UUID> categoryIds,
      @Param("visibility") UserVisibility visibility,
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

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("SELECT i FROM Item i WHERE i.id = :id")
  Optional<Item> findByIdForUpdate(@Param("id") UUID id);

  @Query("select i.state as itemState, COUNT(i.state) as totalItem from Item as i where i.ownerId = :userId group by i.state")
  List<IItemCount> countTotalItemsByOwnerIdAndState(@Param("userId") UUID userId);

  List<Item> findTop3ByOwnerIdAndStateAndModerationStatusOrderByCreatedAtDesc(UUID ownerId, ItemState itemState,
      ModerationStatus status);

  List<Item> findByOwnerIdAndStateAndModerationStatusAndOwnerVisibility(UUID id, ItemState state,
      ModerationStatus moderationStatus, UserVisibility visibility);

  List<Item> findByOwnerIdAndModerationStatus(UUID ownerId, ModerationStatus moderationStatus);

  List<Item> findByOwnerIdIn(List<UUID> userIds);

  List<Item> findByOwnerId(UUID userId);

  boolean existsByIdAndOwnerId(UUID itemId, UUID userId);

  Optional<Item> findById(UUID id);

  Optional<Item> findByIdAndModerationStatus(UUID id, ModerationStatus status);

  boolean existsByIdAndOwnerIdNotAndState(UUID id, UUID userId, ItemState state);

  @Modifying(clearAutomatically = true)
  @Query("Update Item i set i.state = :itemState WHERE i.id = :itemId")
  void updateItemState(@Param("itemId") UUID itemId, @Param("itemState") ItemState itemState);

  @Modifying(clearAutomatically = true)
  @Query("Update Item i set i.ownerVisibility = :visibility WHERE i.ownerId  IN :ownerIds AND i.state != :state")
  void updateOwnerVisibility(@Param("ownerIds") List<UUID> ownerIds, @Param("visibility") UserVisibility visibility,
      @Param("state") ItemState state);

  @Modifying(clearAutomatically = true)
  @Query("Update Item i set i.ownerVisibility = :visibility WHERE i.ownerId  = :ownerId AND i.state != :state")
  void updateOwnerVisibility(@Param("ownerId") UUID ownerId, @Param("visibility") UserVisibility visibility,
      @Param("state") ItemState state);

  @NativeQuery(value = """
      SELECT ii.image_id
      FROM item_images ii
      JOIN items i ON i.id = ii.item_id
      WHERE i.owner_id IN :ownerIds
      """)
  List<UUID> findImageIdsByOwnerIds(@Param("ownerIds") Collection<UUID> ownerIds);

  @Modifying
  @Query("DELETE FROM Item i WHERE i.ownerId IN :ownerIds")
  void deleteAllByOwnerIds(@Param("ownerIds") Collection<UUID> ownerIds);
}
