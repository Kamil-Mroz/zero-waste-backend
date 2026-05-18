package com.kamilpm.zero_waste.service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import com.kamilpm.zero_waste.domain.dto.CategoryTreeDto;
import com.kamilpm.zero_waste.domain.entity.Category;
import com.kamilpm.zero_waste.domain.request.CategoryRequest;

public interface CategoryService {
  List<Category> getAllCategories();

  List<CategoryTreeDto> getCategoryTree();

  Category getCategoryById(UUID categoryId);

  Category createCategory(CategoryRequest categoryRequest);

  Category updateCategory(UUID categoryId, CategoryRequest categoryRequest);

  void deleteCategory(UUID categoryId);

  Map<UUID, Set<UUID>> getCategoryDescendantsCache();

  void invalidateCache();

}
