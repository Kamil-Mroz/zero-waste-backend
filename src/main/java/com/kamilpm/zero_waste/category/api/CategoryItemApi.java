package com.kamilpm.zero_waste.category.api;

import com.kamilpm.zero_waste.category.entity.Category;
import com.kamilpm.zero_waste.category.mapper.CategoryMapper;
import com.kamilpm.zero_waste.category.repository.CategoryRepository;
import com.kamilpm.zero_waste.common.dto.CategoryData;
import com.kamilpm.zero_waste.common.interfaces.CategoryProvider;

import jakarta.persistence.EntityNotFoundException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CategoryItemApi implements CategoryProvider {
  private final CategoryRepository categoryRepository;
  private final CategoryMapper categoryMapper;

  public void existsByCategoryId(UUID id) {
    if (!categoryRepository.existsById(id))
      throw new EntityNotFoundException("Category not found by id " + id);
  }

  public CategoryData getCategoryById(UUID id) {
    return categoryRepository.findById(id).map(categoryMapper::toDataDto)
        .orElseThrow(() -> new EntityNotFoundException("Category not found"));
  }

  @Cacheable(value = "categoryDescendants", key = "#categoryId", cacheManager = "categoryCacheManager")
  public Set<UUID> getCategoryDescendantsById(UUID categoryId) {

    List<Category> categories = categoryRepository.findAll();
    return buildDescendantMap(categories).getOrDefault(categoryId, Collections.emptySet());
  }

  private Map<UUID, Set<UUID>> buildDescendantMap(List<Category> categories) {

    Map<UUID, List<UUID>> children = new HashMap<>();

    for (Category c : categories) {
      if (c.getParent() != null) {
        children
            .computeIfAbsent(c.getParent().getId(), k -> new ArrayList<>())
            .add(c.getId());
      }
    }

    Map<UUID, Set<UUID>> result = new HashMap<>();

    for (Category c : categories) {
      Set<UUID> desc = new HashSet<>();
      collect(c.getId(), children, desc);
      desc.add(c.getId());
      result.put(c.getId(), desc);
    }

    return result;
  }

  private void collect(UUID id,
      Map<UUID, List<UUID>> children,
      Set<UUID> result) {

    List<UUID> kids = children.get(id);
    if (kids == null)
      return;

    for (UUID child : kids) {
      if (result.add(child)) {
        collect(child, children, result);
      }
    }
  }

  public Map<UUID, CategoryData> getCategoriesByIds(Collection<UUID> categoryIds) {
    return categoryRepository.findAllById(categoryIds).stream()
        .collect(Collectors.toMap((category) -> category.getId(), categoryMapper::toDataDto));
  }

}
