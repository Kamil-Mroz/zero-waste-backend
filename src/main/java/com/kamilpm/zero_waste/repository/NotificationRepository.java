package com.kamilpm.zero_waste.repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.kamilpm.zero_waste.domain.entity.Notification;
import com.kamilpm.zero_waste.domain.entity.NotificationType;

import jakarta.transaction.Transactional;

public interface NotificationRepository extends JpaRepository<Notification, UUID> {

  long countByReadFalseAndRecipientId(UUID userId);

  long countByReadFalseAndRecipientIdAndType(UUID userId, NotificationType type);

  @Transactional
  @Modifying
  @Query("update Notification n set n.read = true where n.read = false and n.id = :notificationId and n.recipient.id = :recipientId")
  void markAsRead(@Param( "notificationId") UUID notificationId, @Param( "recipientId") UUID recipientId);

  @Transactional
  @Modifying
  @Query("update Notification n set n.read = true where n.read = false and n.recipient.id = :recipientId")
  void markAllAsRead(@Param("recipientId") UUID recipientId);

  @Query("""
          select n
          from Notification n
          where n.recipient.id = :userId
          and (:type is null or n.type = :type)
          order by n.createdAt desc, n.id desc
      """)
  List<Notification> findFirstPage(
      @Param("userId") UUID userId,
      @Param("type") NotificationType type,
      Pageable pageable);

  @Query("""
          select n
          from Notification n
          where n.recipient.id = :userId
            and (:type is null or n.type = :type)
            and (
                n.createdAt < :createdAt
                or (n.createdAt = :createdAt and n.id < :id)
            )
          order by n.createdAt desc, n.id desc
      """)
  List<Notification> findOlder(
      @Param("userId") UUID userId,
      @Param("type") NotificationType type,
      @Param("createdAt") Instant createdAt,
      @Param("id") UUID id,
      Pageable pageable);

  @Query("""
          select n
          from Notification n
          where n.recipient.id = :userId
            and (:type is null or n.type = :type)
            and (
                n.createdAt > :createdAt
                or (n.createdAt = :createdAt and n.id > :id)
            )
          order by n.createdAt asc, n.id asc
      """)
  List<Notification> findNewer(
      @Param("userId") UUID userId,
      @Param("type") NotificationType type,
      @Param("createdAt") Instant createdAt,
      @Param("id") UUID id,
      Pageable pageable);

  Optional<Notification> findByIdAndRecipient_Id(UUID notificationId, UUID userId);

  void deleteByRecipient_IdIn(List<UUID> ids);
}
