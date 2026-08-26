package com.kamilpm.zero_waste.repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.kamilpm.zero_waste.domain.entity.Item;
import com.kamilpm.zero_waste.domain.entity.ItemState;
import com.kamilpm.zero_waste.domain.entity.ModerationStatus;
import com.kamilpm.zero_waste.domain.interfaces.IItemCount;

import jakarta.persistence.LockModeType;

@Repository
public interface ItemRepository extends JpaRepository<Item, UUID> {
  @EntityGraph(attributePaths = { "owner", "category", "images", "thumbnail" })
  @Query("""
      SELECT DISTINCT i
      FROM Item i
      WHERE i.state = :state
      AND (:ownerId IS NULL OR i.owner.id != :ownerId)
      AND i.moderationStatus = :moderationStatus
      AND (:categoryIds IS NULL OR i.category.id IN :categoryIds)
      AND (i.owner.banActive = false)
      AND (i.owner.role != 'DEMO')
      AND (:text IS NULL
        OR LOWER(i.title) LIKE :text ESCAPE '\\'
        OR LOWER(i.city) LIKE :text ESCAPE '\\'
      )
        order by i.createdAt desc
        """)
  Page<Item> searchItems(@Param("ownerId") UUID ownerId, @Param("state") ItemState state, @Param("text") String text,
      @Param("moderationStatus") ModerationStatus moderationStatus, @Param("categoryIds") Set<UUID> categoryIds,
      Pageable pageable);

  @EntityGraph(attributePaths = { "owner", "category", "images", "thumbnail" })
  @Query("""
        SELECT DISTINCT i
        FROM Item i
        WHERE i.owner.id = :ownerId
        AND (:categoryIds IS NULL OR i.category.id IN :categoryIds)
        AND (:text IS NULL
        OR LOWER(i.title) LIKE :text ESCAPE '\\'
        OR LOWER(i.city) LIKE :text ESCAPE '\\'
        )
        AND i.state IN :states
        order by i.createdAt desc
      """)
  Page<Item> findOwnItems(@Param("ownerId") UUID ownerId, @Param("text") String text,
      @Param("categoryIds") Set<UUID> categoryIds, @Param("states") List<ItemState> states, Pageable pageable);

  boolean existsByCategory_Id(UUID id);

  @EntityGraph(attributePaths = { "owner", "category", "images", "thumbnail" })
  @Query("SELECT DISTINCT i FROM Item i WHERE i.id = ?1")
  Optional<Item> findByIdWithOwnerAndCategoryAndImages(UUID id);

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @EntityGraph(attributePaths = { "owner", "thumbnail" })
  @Query("SELECT i FROM Item i WHERE i.id = :id")
  Optional<Item> findByIdForUpdate(@Param("id") UUID id);

  @EntityGraph(attributePaths = { "owner" })
  int countByOwner_Id(UUID userId);

  @EntityGraph(attributePaths = { "owner", })
  @Query("select i.state as itemState, COUNT(i.state) as totalItem from Item as i where i.owner.id = :userId group by i.state")
  List<IItemCount> countTotalItemsByOwnerIdAndState(@Param("userId") UUID userId);

  @EntityGraph(attributePaths = { "owner", "category", "images", "thumbnail" })
  List<Item> findTop3ByOwner_IdAndStateAndModerationStatusOrderByCreatedAtDesc(UUID ownerId, ItemState itemState,
      ModerationStatus status);

  @EntityGraph(attributePaths = { "owner", "category", "images", "thumbnail" })
  List<Item> findByOwner_IdAndStateAndModerationStatus(UUID id, ItemState state, ModerationStatus moderationStatus);

  @EntityGraph(attributePaths = { "owner", "images", "thumbnail" })
  List<Item> findByOwner_IdAndModerationStatus(UUID ownerId, ModerationStatus moderationStatus);

  @EntityGraph(attributePaths = { "owner", "images", "thumbnail" })
  List<Item> findByOwnerIdIn(List<UUID> userIds);

  @EntityGraph(attributePaths = { "owner", })
  boolean existsByIdAndOwner_Id(UUID itemId, UUID userId);

  @EntityGraph(attributePaths = { "owner", "thumbnail" })
  Optional<Item> findById(UUID id);

  @EntityGraph(attributePaths = { "owner", "thumbnail" })
  Optional<Item> findByIdAndModerationStatus(UUID id, ModerationStatus status);

  @EntityGraph(attributePaths = { "owner" })
  boolean existsByIdAndOwner_IdNotAndState(UUID id, UUID userId, ItemState state);
}
