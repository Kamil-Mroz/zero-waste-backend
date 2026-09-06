package com.kamilpm.zero_waste.category.api;

import com.kamilpm.zero_waste.category.mapper.CategoryMapper;
import com.kamilpm.zero_waste.category.repository.CategoryRepository;
import com.kamilpm.zero_waste.category.service.CategoryService;

import jakarta.persistence.EntityNotFoundException;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CategoryItemApi {
  private final CategoryRepository categoryRepository;
  private final CategoryService categoryService;
  private final CategoryMapper categoryMapper;

  public void existsByCategoryId(UUID id) {
    if (!categoryRepository.existsById(id))
      throw new EntityNotFoundException("Category not found by id " + id);
  }

  public CategoryDto getCategoryById(UUID id) {
    return categoryRepository.findById(id).map(categoryMapper::toDto)
        .orElseThrow(() -> new EntityNotFoundException("Category not found"));
  }

  public Set<UUID> getCategoryDescendantsById(UUID categoryId) {
    return categoryService.getCategoryDescendantsById(categoryId);
  }

  public Map<UUID, CategoryDto> getCategoriesByIds(Collection<UUID> categoryIds) {
    return categoryRepository.findAllById(categoryIds).stream()
        .collect(Collectors.toMap((category) -> category.getId(), categoryMapper::toDto));
  }

}
