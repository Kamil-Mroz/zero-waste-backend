package com.kamilpm.zero_waste.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.kamilpm.zero_waste.domain.entity.Image;

import java.util.List;
import java.util.Optional;

public interface ImageRepository extends JpaRepository<Image, UUID> {

  Optional<Image> findByStoredName(String storedName);

  @Modifying
  @Query("DELETE FROM Image i WHERE i.id IN ?2 AND i.item.id = ?1")
  void deleteImagesByItem(UUID itemId, List<UUID> imageIds);

  List<Image> findByItem_IdAndIdIn(UUID itemId, List<UUID> ids);

}
