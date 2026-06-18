package com.kamilpm.zero_waste.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.kamilpm.zero_waste.domain.entity.Review;
import com.kamilpm.zero_waste.domain.interfaces.IRatingBreakdownWithStats;
import com.kamilpm.zero_waste.domain.interfaces.IRatingCountProjection;

@Repository
public interface ReviewRepository extends JpaRepository<Review, UUID> {

  @EntityGraph(attributePaths = { "offer" })
  boolean existsByOffer_Id(UUID offerId);

  @EntityGraph(attributePaths = { "reviewee" })
  Page<Review> findByReviewee_IdOrderByCreatedAtDesc(UUID revieweeId, Pageable pageable);

  @EntityGraph(attributePaths = { "reviewer" })
  Page<Review> findByReviewer_Id(UUID reviewerId, Pageable pageable);

  @EntityGraph(attributePaths = { "reviewee" })
  List<Review> findTop3ByReviewee_IdOrderByCreatedAtDesc(UUID revieweeId);

  @EntityGraph(attributePaths = { "reviewee" })
  @Query("""
        SELECT AVG(r.rating)
        FROM Review r
        WHERE r.reviewee.id = :userId
      """)
  Double getAverageRating(UUID userId);

  long countByReviewee_Id(UUID userId);

  @EntityGraph(attributePaths = { "reviewee" })
  @Query("""
        SELECT
          r.rating as rating,
          COUNT(r) as count
        FROM Review r
        WHERE r.reviewee.id = :userId
        GROUP BY r.rating
        ORDER BY r.rating DESC
      """)
  List<IRatingCountProjection> getRatingBreakdown(UUID userId);

  @EntityGraph(attributePaths = { "reviewee" })
  @Query("""
        SELECT
          r.rating as rating,
          COUNT(r) as count,
          AVG(r.rating) as avgRating,
          COUNT(*) as totalCount
        FROM Review r
        WHERE r.reviewee.id = :userId
        GROUP BY r.rating
        ORDER BY r.rating DESC
      """)
  List<IRatingBreakdownWithStats> getRatingBreakdownWithStats(UUID userId);

  void deleteByReviewer_IdIn(List<UUID> ids);

  void deleteByReviewee_IdIn(List<UUID> ids);
}
