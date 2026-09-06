package com.kamilpm.zero_waste.review.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.kamilpm.zero_waste.common.entity.ModerationStatus;
import com.kamilpm.zero_waste.common.interfaces.IRatingBreakdownWithStats;
import com.kamilpm.zero_waste.review.entity.Review;
import com.kamilpm.zero_waste.review.interfaces.IRatingCountProjection;

@Repository
public interface ReviewRepository extends JpaRepository<Review, UUID> {

  boolean existsByOfferId(UUID offerId);

  Page<Review> findByRevieweeIdAndModerationStatusOrderByCreatedAtDesc(UUID revieweeId, ModerationStatus status,
      Pageable pageable);

  Page<Review> findByReviewerId(UUID reviewerId, Pageable pageable);

  List<Review> findTop3ByRevieweeIdAndModerationStatusOrderByCreatedAtDesc(UUID revieweeId, ModerationStatus status);

  @Query("""
        SELECT AVG(r.rating)
        FROM Review r
        WHERE r.revieweeId = :userId
      """)
  Double getAverageRating(@Param("userId") UUID userId);

  long countByRevieweeId(UUID userId);

  @Query("""
        SELECT
          r.rating as rating,
          COUNT(r) as count
        FROM Review r
        WHERE r.revieweeId = :userId
        GROUP BY r.rating
        ORDER BY r.rating DESC
      """)
  List<IRatingCountProjection> getRatingBreakdown(@Param("userId") UUID userId);

  @Query("""
        SELECT
          r.rating as rating,
          COUNT(r) as count,
          AVG(r.rating) as avgRating,
          COUNT(*) as totalCount
        FROM Review r
        WHERE r.revieweeId = :userId
        GROUP BY r.rating
        ORDER BY r.rating DESC
      """)
  List<IRatingBreakdownWithStats> getRatingBreakdownWithStats(@Param("userId") UUID userId);

  void deleteByReviewerIdIn(List<UUID> ids);

  void deleteByRevieweeIdIn(List<UUID> ids);

  void deleteByOfferIdIn(Collection<UUID> ids);

  @Modifying
  @Query("DELETE FROM Review r WHERE r.offerId = :offerId")

  void deleteByOfferId(@Param("offerId") UUID offerId);

  boolean existsByIdAndReviewerIdNotAndRevieweeId(UUID subjectId, UUID reviewerId, UUID revieweeId);

  boolean existsByIdAndReviewerId(UUID reviewId, UUID reviewerId);

  boolean existsByIdAndRevieweeId(UUID reviewId, UUID revieweeId);

  Optional<Review> findById(UUID id);

  Optional<Review> findByIdAndModerationStatus(UUID id, ModerationStatus moderationStatus);

  @Query("""
      SELECT CASE WHEN COUNT(r) > 0 THEN true ELSE false END
      FROM Review r
      WHERE r.id = :reviewId
        AND (r.reviewerId = :userId OR r.revieweeId = :userId)
      """)
  boolean isReviewerOrReviewee(@Param("reviewId") UUID reviewId, @Param("userId") UUID userId);

}
