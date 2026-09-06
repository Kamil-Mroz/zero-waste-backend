package com.kamilpm.zero_waste.category.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import com.kamilpm.zero_waste.category.api.CategoryTreeDto;
import com.kamilpm.zero_waste.category.dto.CategoryRequest;
import com.kamilpm.zero_waste.category.entity.Category;
import com.kamilpm.zero_waste.category.mapper.CategoryMapper;
import com.kamilpm.zero_waste.category.repository.CategoryRepository;
import com.kamilpm.zero_waste.common.exception.ConflictException;
import com.kamilpm.zero_waste.common.exception.EntityNotFoundException;
import com.kamilpm.zero_waste.item.api.ItemCategoryApi;

import io.jsonwebtoken.lang.Collections;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class CategoryService {
  private final CategoryRepository categoryRepository;
  private final ItemCategoryApi itemCategoryApi;
  private final CategoryMapper categoryMapper;

  public List<Category> getAllCategories() {
    return categoryRepository.findAll();
  }

  @Cacheable("categoryTree")
  public List<CategoryTreeDto> getCategoryTree() {
    return buildTree();
  }

  private List<CategoryTreeDto> buildTree() {
    List<Category> categories = categoryRepository.findAll();

    Map<UUID, CategoryTreeDto> map = new HashMap<>();

    for (Category category : categories) {
      CategoryTreeDto dto = categoryMapper.toTreeDto(category);

      if (dto.getChildren() == null) {
        dto.setChildren(new ArrayList<>());
      }

      map.put(category.getId(), dto);
    }

    List<CategoryTreeDto> roots = new ArrayList<>();

    for (Category category : categories) {
      CategoryTreeDto dto = map.get(category.getId());

      if (category.getParent() == null) {
        roots.add(dto);
      } else {
        CategoryTreeDto parent = map.get(category.getParent().getId());

        if (parent != null) {
          parent.getChildren().add(dto);
        }
      }
    }

    return roots;
  }

  public Category getCategoryById(UUID categoryId) {

    return categoryRepository.findById(categoryId)
        .orElseThrow(() -> new EntityNotFoundException("Category not found by id " + categoryId));
  }

  @Transactional
  public Category createCategory(CategoryRequest categoryRequest) {
    Category parent = null;
    if (categoryRepository.existsByName(categoryRequest.getName())) {
      throw new ConflictException("Category already exists with name " + categoryRequest.getName(), "name");
    }

    if (categoryRequest.getCategoryId() != null) {
      parent = categoryRepository.findById(categoryRequest.getCategoryId())
          .orElseThrow(() -> new EntityNotFoundException("Parent category not found"));
    }
    Category category = Category.builder().name(categoryRequest.getName()).parent(parent).build();

    Category savedCategory = categoryRepository.save(category);

    invalidateCache();

    return savedCategory;
  }

  @Transactional
  public Category updateCategory(UUID categoryId, CategoryRequest categoryRequest) {
    Category parent = null;

    if (categoryRepository.existsByNameAndIdNot(categoryRequest.getName(), categoryId)) {
      throw new ConflictException("Category already exists with name " + categoryRequest.getName(), "name");
    }

    Category category = categoryRepository.findById(categoryId)
        .orElseThrow(() -> new EntityNotFoundException("Category not found by id" + categoryId));

    // boolean isSameName = Objects.equals(category.getName(),
    // categoryRequest.getName());
    // UUID currentParentId = category.getParent() != null ?
    // category.getParent().getId() : null;
    // boolean isSameParent = Objects.equals(currentParentId,
    // categoryRequest.getCategoryId());

    // if (isSameName && isSameParent) {
    // return category;
    // }

    if (categoryRequest.getCategoryId() != null) {
      parent = categoryRepository.findById(categoryRequest.getCategoryId())
          .orElseThrow(() -> new EntityNotFoundException(
              "Parent category not found with id " + categoryRequest.getCategoryId()));
      validateNoCycle(category, parent);
    }

    category.setParent(parent);
    category.setName(categoryRequest.getName());

    Category updatedCategory = categoryRepository.save(category);

    invalidateCache();
    return updatedCategory;
  }

  private void validateNoCycle(Category category, Category newParent) {
    Category current = newParent;
    while (current != null) {
      if (Objects.equals(current.getId(), category.getId())) {
        throw new IllegalStateException("Cannot set a child as parent (cycle detected)");
      }
      current = current.getParent();
    }
  }

  @Transactional
  public void deleteCategory(UUID categoryId) {
    if (!categoryRepository.existsById(categoryId)) {

      throw new EntityNotFoundException("Category not found");
    }

    if (categoryRepository.existsByParentId(categoryId)) {
      throw new ConflictException("Category can not be deleted cause of children categories");
    }

    if (itemCategoryApi.existsByCategoryId(categoryId)) {
      throw new ConflictException("Category can not be deleted cause of existing items in category");
    }

    categoryRepository.deleteById(categoryId);
    invalidateCache();

  }

  @Cacheable(value = "categoryDescendants", key = "#categoryId")
  public Set<UUID> getCategoryDescendantsById(UUID categoryId) {
    log.info("BUILDING CATEGORY DESCENDATS FOR ID {}  FROM DATABASE", categoryId);

    // return getCategoryDescendantsCache().get(categoryId)
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

  @CacheEvict(value = { "categoryTree", "categoryDescendants" }, allEntries = true)
  private void invalidateCache() {
  }

}
