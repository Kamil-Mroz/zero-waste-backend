package com.kamilpm.zero_waste.image.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.kamilpm.zero_waste.image.entity.Image;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface ImageRepository extends JpaRepository<Image, UUID> {

  Optional<Image> findByStoredName(String storedName);

  @Modifying
  @Query("DELETE FROM Image i WHERE i.id IN ?2 AND i.itemId = ?1")
  void deleteImagesByItem(UUID itemId, List<UUID> imageIds);

  List<Image> findByItemIdAndIdIn(UUID itemId, List<UUID> ids);

  List<Image> findByItemIdIn(Collection<UUID> itemIds);

  @Modifying
  @Query("DELETE FROM Image i WHERE  i.itemId = :itemId")
  void deleteAllByItemId(@Param("itemId") UUID itemId);

  List<Image> findAllByItemId(UUID id);
}
