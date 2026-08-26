package com.kamilpm.zero_waste.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.kamilpm.zero_waste.domain.entity.Blog;
import com.kamilpm.zero_waste.domain.entity.ModerationStatus;
import com.kamilpm.zero_waste.domain.entity.UserRole;

@Repository
public interface BlogRepository extends JpaRepository<Blog, UUID> {

  @EntityGraph(attributePaths = { "author", })
  List<Blog> findByModerationStatusAndAuthorBanActiveFalseAndAuthorRoleNotOrderByCreatedAtDesc(
      ModerationStatus moderationStatus, UserRole role);

  @EntityGraph(attributePaths = { "author", })
  Optional<Blog> findById(UUID id);

  @EntityGraph(attributePaths = { "author" })
  Optional<Blog> findByIdAndModerationStatus(UUID id, ModerationStatus status);

  @EntityGraph(attributePaths = { "author", })
  List<Blog> findByAuthor_IdOrderByCreatedAtDesc(UUID id);

  @EntityGraph(attributePaths = { "author", })
  Optional<Blog> findByIdAndAuthor_IdAndModerationStatus(UUID blogId, UUID authorId, ModerationStatus moderationStatus);

  @EntityGraph(attributePaths = { "author" })
  void deleteByAuthor_IdIn(List<UUID> ids);

  @EntityGraph(attributePaths = { "author" })
  boolean existsByIdAndAuthor_IdNot(UUID id, UUID userId);

  @EntityGraph(attributePaths = { "author" })
  boolean existsByIdAndAuthor_Id(UUID blogId, UUID userId);
}
