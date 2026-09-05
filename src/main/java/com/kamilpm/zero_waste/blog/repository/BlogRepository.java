package com.kamilpm.zero_waste.blog.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.kamilpm.zero_waste.blog.entity.Blog;
import com.kamilpm.zero_waste.common.entity.ModerationStatus;

@Repository
public interface BlogRepository extends JpaRepository<Blog, UUID> {

  @Query("""
      SELECT b
      FROM Blog b
      where b.moderationStatus = :status
        AND b.authorId NOT IN :excludedAuthorIds
      ORDER BY b.createdAt DESC
      """)
  List<Blog> findVisibleBlogsExcludingAuthors(ModerationStatus status, Collection<UUID> excludedAuthorIds);

  Optional<Blog> findById(UUID id);

  Optional<Blog> findByIdAndModerationStatus(UUID id, ModerationStatus status);

  List<Blog> findByAuthorIdOrderByCreatedAtDesc(UUID id);

  Optional<Blog> findByIdAndAuthorIdAndModerationStatus(UUID blogId, UUID authorId, ModerationStatus moderationStatus);

  void deleteByAuthorIdIn(List<UUID> ids);

  boolean existsByIdAndAuthorIdNot(UUID id, UUID userId);

  boolean existsByIdAndAuthorId(UUID blogId, UUID userId);

}
