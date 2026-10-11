package com.kamilpm.zero_waste.image.repository;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.kamilpm.zero_waste.image.entity.PendingImageUpload;

@Repository
public interface PendingImageUploadRepository extends JpaRepository<PendingImageUpload, UUID> {
  List<PendingImageUpload> findAllByIdIn(Collection<UUID> ids);

  List<PendingImageUpload> findAllByStatusAndExpiresAtBefore(PendingImageUpload.Status status, Instant expiresAt);

}
