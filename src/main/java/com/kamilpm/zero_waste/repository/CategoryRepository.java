package com.kamilpm.zero_waste.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.kamilpm.zero_waste.domain.entity.Category;

@Repository
public interface CategoryRepository extends JpaRepository<Category, UUID> {

  Optional<Category> findByName(String name);

  Boolean existsByName(String name);

  Boolean existsByNameAndIdNot(String name, UUID id);

  Boolean existsByParentId(UUID id);

}
