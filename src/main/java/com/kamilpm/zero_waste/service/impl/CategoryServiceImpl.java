package com.kamilpm.zero_waste.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.kamilpm.zero_waste.domain.dto.CategoryTreeDto;
import com.kamilpm.zero_waste.domain.entity.Category;
import com.kamilpm.zero_waste.domain.mapper.CategoryMapper;
import com.kamilpm.zero_waste.domain.request.CategoryRequest;
import com.kamilpm.zero_waste.exception.ConflictException;
import com.kamilpm.zero_waste.exception.EntityNotFoundException;
import com.kamilpm.zero_waste.repository.CategoryRepository;
import com.kamilpm.zero_waste.repository.ItemRepository;
import com.kamilpm.zero_waste.service.CategoryService;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {
  private final CategoryRepository categoryRepository;
  private final ItemRepository itemRepository;
  private final CategoryMapper categoryMapper;

  private List<CategoryTreeDto> cachedTree;
  private Map<UUID, Set<UUID>> categoryDescendantsCache;

  @Override
  public List<Category> getAllCategories() {
    return categoryRepository.findAll();
  }

  @Override
  public List<CategoryTreeDto> getCategoryTree() {
    if (cachedTree == null) {
      cachedTree = buildTree();
    }
    return cachedTree;
  }

  private List<CategoryTreeDto> buildTree() {
    List<Category> categories = categoryRepository.findAll();
    Map<UUID, CategoryTreeDto> map = new HashMap<>();

    for (Category c : categories) {
      map.put(c.getId(), categoryMapper.toTreeDto(c));
    }

    List<CategoryTreeDto> roots = new ArrayList<>();

    for (Category c : categories) {
      CategoryTreeDto dto = map.get(c.getId());

      if (c.getParent() == null) {
        roots.add(dto);
      } else {
        map.get(c.getParent().getId()).getChildren().add(dto);
      }
    }
    return roots;
  }

  @Override
  public Category getCategoryById(UUID categoryId) {

    return categoryRepository.findById(categoryId)
        .orElseThrow(() -> new EntityNotFoundException("Category not found by id " + categoryId));
  }

  @Transactional
  @Override
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
  @Override
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
  @Override
  public void deleteCategory(UUID categoryId) {
    if (!categoryRepository.existsById(categoryId)) {

      throw new EntityNotFoundException("Category not found");
    }

    if (categoryRepository.existsByParentId(categoryId)) {
      throw new ConflictException("Category can not be deleted cause of children categories");
    }

    if (itemRepository.existsByCategory_Id(categoryId)) {
      throw new ConflictException("Category can not be deleted cause of existing items in category");
    }

    categoryRepository.deleteById(categoryId);
    invalidateCache();

  }

  @Override
  public Set<UUID> getCategoryDescendantsById(UUID categoryId) {

    return getCategoryDescendantsCache().get(categoryId);
  }

  private Map<UUID, Set<UUID>> getCategoryDescendantsCache() {
    if (categoryDescendantsCache == null) {
      List<Category> categories = categoryRepository.findAll();
      categoryDescendantsCache = buildDescendantMap(categories);
    }
    return categoryDescendantsCache;
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

  private void invalidateCache() {
    cachedTree = null;
    categoryDescendantsCache = null;
  }

}
