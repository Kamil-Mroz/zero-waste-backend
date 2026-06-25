package com.kamilpm.zero_waste.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.kamilpm.zero_waste.domain.entity.Blog;

@Repository
public interface BlogRepository extends JpaRepository<Blog, UUID> {

  @EntityGraph(attributePaths = { "author" })
  List<Blog> findByAuthor_Id(UUID id);
}
