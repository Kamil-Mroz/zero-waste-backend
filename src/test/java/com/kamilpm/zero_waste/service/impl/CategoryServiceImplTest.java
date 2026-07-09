package com.kamilpm.zero_waste.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.kamilpm.zero_waste.domain.dto.CategoryTreeDto;
import com.kamilpm.zero_waste.domain.entity.Category;
import com.kamilpm.zero_waste.domain.mapper.CategoryMapper;
import com.kamilpm.zero_waste.domain.request.CategoryRequest;
import com.kamilpm.zero_waste.exception.ConflictException;
import com.kamilpm.zero_waste.exception.EntityNotFoundException;
import com.kamilpm.zero_waste.repository.CategoryRepository;
import com.kamilpm.zero_waste.repository.ItemRepository;

@ExtendWith(MockitoExtension.class)
@DisplayName("CategoryServiceImpl Unit Tests")
public class CategoryServiceImplTest {

  @Mock
  private CategoryRepository categoryRepository;

  @Mock
  private ItemRepository itemRepository;

  @Mock
  private CategoryMapper categoryMapper;

  @InjectMocks
  private CategoryServiceImpl categoryService;

  private Category parentCategory;
  private Category parentUpdateCategory;
  private Category category;
  private Category categoryWithParent;
  private Category categoryUpdate;
  private Category categoryUpdateWithParent;
  private CategoryRequest categoryRequest;
  private CategoryRequest categoryRequestWithParent;
  private CategoryRequest categoryUpdateRequest;
  private CategoryRequest categoryUpdateRequestWithParent;
  private CategoryTreeDto parentCategoryWithChildren;
  private CategoryTreeDto categoryWithParentWithChildren;
  private CategoryTreeDto categoryUpdateWithChildren;

  @BeforeEach
  void Setup() {
    this.parentCategory = Category.builder()
        .id(UUID.fromString("4f81fd62-f90b-45af-b5ab-ee9ab5f46283"))
        .name("parent")
        .parent(null)
        .build();

    this.parentUpdateCategory = Category.builder()
        .id(UUID.fromString("4f81fd62-f90b-45af-b5ab-ee9ab5f46321"))
        .name("parent update")
        .parent(null)
        .build();

    this.category = Category.builder()
        .name("category")
        .parent(null)
        .build();

    this.categoryWithParent = Category.builder()
        .name("categoryWithParent")
        .parent(this.parentCategory)
        .build();

    this.categoryRequest = CategoryRequest.builder()
        .categoryId(null)
        .name("category")
        .build();

    this.categoryRequestWithParent = CategoryRequest.builder()
        .categoryId(this.parentCategory.getId())
        .name("categoryWithParent")
        .build();

    this.categoryUpdateRequest = CategoryRequest.builder()
        .categoryId(null)
        .name("updated category")
        .build();

    this.categoryUpdate = Category.builder()
        .parent(null)
        .name("updated category")
        .build();

    this.categoryUpdateRequestWithParent = CategoryRequest.builder()
        .categoryId(this.parentUpdateCategory.getId())
        .name("updated category")
        .build();

    this.categoryUpdateWithParent = Category.builder()
        .parent(this.parentUpdateCategory)
        .name("updated category")
        .build();

    this.parentCategoryWithChildren = CategoryTreeDto.builder()
        .id(parentCategory.getId())
        .name(parentCategory.getName())
        .build();

    this.categoryWithParentWithChildren = CategoryTreeDto.builder()
        .id(categoryWithParent.getId())
        .name(categoryWithParent.getName())
        .build();

    this.categoryUpdateWithChildren = CategoryTreeDto.builder()
        .id(categoryUpdate.getId())
        .name(categoryUpdate.getName())
        .build();
  }

  @Nested
  @DisplayName("Create Categories")
  class CreateCategories {
    @Test
    @DisplayName("Should create category successfully when valid request and category exists")
    void test1() {
      // Given

      when(CategoryServiceImplTest.this.categoryRepository.existsByName(
          CategoryServiceImplTest.this.categoryRequest.getName())).thenReturn(false);

      when(CategoryServiceImplTest.this.categoryRepository.save(any(Category.class)))
          .thenReturn(CategoryServiceImplTest.this.category);
      // When

      final Category result = CategoryServiceImplTest.this.categoryService
          .createCategory(CategoryServiceImplTest.this.categoryRequest);
      // Then
      assertNotNull(result);
      assertEquals("category", result.getName());
      verify(CategoryServiceImplTest.this.categoryRepository)
          .existsByName(CategoryServiceImplTest.this.categoryRequest.getName());

      verify(CategoryServiceImplTest.this.categoryRepository).save(CategoryServiceImplTest.this.category);

      verify(CategoryServiceImplTest.this.categoryRepository, times(0))
          .findById(CategoryServiceImplTest.this.category.getId());
    }

    @Test
    @DisplayName("Should create category with parent successfully when valid request and category exists")
    void test4() {
      // Given

      when(CategoryServiceImplTest.this.categoryRepository.existsByName(
          CategoryServiceImplTest.this.categoryRequestWithParent.getName())).thenReturn(false);

      when(CategoryServiceImplTest.this.categoryRepository
          .findById(CategoryServiceImplTest.this.categoryRequestWithParent.getCategoryId()))
          .thenReturn(Optional.of(CategoryServiceImplTest.this.parentCategory));

      when(CategoryServiceImplTest.this.categoryRepository.save(any(Category.class)))
          .thenReturn(CategoryServiceImplTest.this.categoryWithParent);
      // When

      final Category result = CategoryServiceImplTest.this.categoryService
          .createCategory(CategoryServiceImplTest.this.categoryRequestWithParent);
      // Then
      assertNotNull(result);
      assertEquals("categoryWithParent", result.getName());
      verify(CategoryServiceImplTest.this.categoryRepository)
          .existsByName(CategoryServiceImplTest.this.categoryRequestWithParent.getName());

      verify(CategoryServiceImplTest.this.categoryRepository)
          .save(CategoryServiceImplTest.this.categoryWithParent);

      verify(CategoryServiceImplTest.this.categoryRepository)
          .findById(CategoryServiceImplTest.this.categoryRequestWithParent.getCategoryId());
    }

    @Test
    @DisplayName("Should should throw ConflictException when category name already taken")
    void test2() {

      when(CategoryServiceImplTest.this.categoryRepository.existsByName(
          CategoryServiceImplTest.this.categoryRequest.getName())).thenReturn(true);
      final ConflictException exception = assertThrows(ConflictException.class,
          () -> CategoryServiceImplTest.this.categoryService
              .createCategory(CategoryServiceImplTest.this.categoryRequest));
      assertEquals("Category already exists with name " + CategoryServiceImplTest.this.categoryRequest.getName(),
          exception.getMessage());
      assertEquals("name", exception.getFieldError());

      verify(CategoryServiceImplTest.this.categoryRepository)
          .existsByName(CategoryServiceImplTest.this.categoryRequest.getName());

      verify(CategoryServiceImplTest.this.categoryRepository, times(0)).save(CategoryServiceImplTest.this.category);

      verify(CategoryServiceImplTest.this.categoryRepository, times(0))
          .findById(CategoryServiceImplTest.this.category.getId());
    }

    @Test
    @DisplayName("Should should throw EntityNotFoundException when category by id not found")
    void test3() {

      when(CategoryServiceImplTest.this.categoryRepository.existsByName(
          CategoryServiceImplTest.this.categoryRequestWithParent.getName())).thenReturn(false);

      when(CategoryServiceImplTest.this.categoryRepository.findById(
          CategoryServiceImplTest.this.categoryRequestWithParent.getCategoryId())).thenReturn(Optional.empty());
      final EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
          () -> CategoryServiceImplTest.this.categoryService
              .createCategory(CategoryServiceImplTest.this.categoryRequestWithParent));
      assertEquals("Parent category not found", exception.getMessage());

      verify(CategoryServiceImplTest.this.categoryRepository)
          .existsByName(CategoryServiceImplTest.this.categoryRequestWithParent.getName());

      verify(CategoryServiceImplTest.this.categoryRepository)
          .findById(CategoryServiceImplTest.this.parentCategory.getId());

      verify(CategoryServiceImplTest.this.categoryRepository, times(0))
          .save(CategoryServiceImplTest.this.categoryWithParent);

    }
  }

  @Nested
  @DisplayName("Update Categories")
  class UpdateCategories {

    @Test
    @DisplayName("Should throw ConflictException if category name was taken")
    void test1() {

      final UUID categoryId = UUID.fromString("4f81fd62-f90b-45af-b5ab-ee9ab5f46123");
      when(CategoryServiceImplTest.this.categoryRepository.existsByNameAndIdNot(
          CategoryServiceImplTest.this.categoryUpdateRequest.getName(), categoryId)).thenReturn(true);
      final ConflictException exception = assertThrows(ConflictException.class,
          () -> CategoryServiceImplTest.this.categoryService.updateCategory(categoryId,
              CategoryServiceImplTest.this.categoryUpdateRequest));
      assertNotNull(exception);
      assertEquals("Category already exists with name " + CategoryServiceImplTest.this.categoryUpdateRequest.getName(),
          exception.getMessage());
      assertEquals("name", exception.getFieldError());

      verify(CategoryServiceImplTest.this.categoryRepository)
          .existsByNameAndIdNot(CategoryServiceImplTest.this.categoryUpdateRequest.getName(), categoryId);

      verify(CategoryServiceImplTest.this.categoryRepository, times(0))
          .findById(categoryId);

      verify(CategoryServiceImplTest.this.categoryRepository, times(0))
          .findById(CategoryServiceImplTest.this.categoryUpdateRequest.getCategoryId());

      verify(CategoryServiceImplTest.this.categoryRepository, times(0))
          .save(CategoryServiceImplTest.this.categoryUpdate);
    }

    @Test
    @DisplayName("Should throw EntityNotFoundException if invalid categoryId")
    void test2() {

      final UUID categoryId = UUID.fromString("4f81fd62-f90b-45af-b5ab-ee9ab5f46123");

      when(CategoryServiceImplTest.this.categoryRepository.existsByNameAndIdNot(
          CategoryServiceImplTest.this.categoryUpdateRequest.getName(), categoryId)).thenReturn(false);

      when(CategoryServiceImplTest.this.categoryRepository.findById(
          categoryId)).thenReturn(Optional.empty());

      final EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
          () -> CategoryServiceImplTest.this.categoryService.updateCategory(categoryId,
              CategoryServiceImplTest.this.categoryUpdateRequest));
      assertNotNull(exception);
      assertEquals("Category not found by id" + categoryId,
          exception.getMessage());

      verify(CategoryServiceImplTest.this.categoryRepository)
          .existsByNameAndIdNot(CategoryServiceImplTest.this.categoryUpdateRequest.getName(), categoryId);

      verify(CategoryServiceImplTest.this.categoryRepository)
          .findById(categoryId);

      verify(CategoryServiceImplTest.this.categoryRepository, times(0))
          .findById(CategoryServiceImplTest.this.categoryUpdateRequest.getCategoryId());

      verify(CategoryServiceImplTest.this.categoryRepository, times(0))
          .save(CategoryServiceImplTest.this.categoryUpdate);
    }

    @Test
    @DisplayName("Should throw EntityNotFoundException if invalid parentId")
    void test3() {

      final UUID categoryId = UUID.fromString("4f81fd62-f90b-45af-b5ab-ee9ab5f46123");

      when(CategoryServiceImplTest.this.categoryRepository.existsByNameAndIdNot(
          CategoryServiceImplTest.this.categoryUpdateRequest.getName(), categoryId)).thenReturn(false);

      when(CategoryServiceImplTest.this.categoryRepository.findById(
          categoryId)).thenReturn(Optional.of(CategoryServiceImplTest.this.categoryUpdate));

      when(CategoryServiceImplTest.this.categoryRepository.findById(
          CategoryServiceImplTest.this.categoryUpdateRequestWithParent.getCategoryId())).thenReturn(Optional.empty());

      final EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
          () -> CategoryServiceImplTest.this.categoryService.updateCategory(categoryId,
              CategoryServiceImplTest.this.categoryUpdateRequestWithParent));
      assertNotNull(exception);
      assertEquals("Parent category not found with id "
          + CategoryServiceImplTest.this.categoryUpdateRequestWithParent.getCategoryId(), exception.getMessage());

      verify(CategoryServiceImplTest.this.categoryRepository)
          .existsByNameAndIdNot(CategoryServiceImplTest.this.categoryUpdateRequest.getName(), categoryId);

      verify(CategoryServiceImplTest.this.categoryRepository)
          .findById(categoryId);

      verify(CategoryServiceImplTest.this.categoryRepository)
          .findById(CategoryServiceImplTest.this.categoryUpdateRequestWithParent.getCategoryId());

      verify(CategoryServiceImplTest.this.categoryRepository, times(0))
          .save(CategoryServiceImplTest.this.categoryUpdate);

    }

    @Test
    @DisplayName("Should throw IllegalStateException when a cycle is detected")
    void test4() {

      final UUID categoryId = UUID.fromString("4f81fd62-f90b-45af-b5ab-ee9ab5f46123");

      CategoryServiceImplTest.this.categoryUpdate.setId(categoryId);
      when(CategoryServiceImplTest.this.categoryRepository.existsByNameAndIdNot(
          CategoryServiceImplTest.this.categoryUpdateRequest.getName(), categoryId)).thenReturn(false);

      when(CategoryServiceImplTest.this.categoryRepository.findById(
          categoryId)).thenReturn(Optional.of(CategoryServiceImplTest.this.categoryUpdate));

      when(CategoryServiceImplTest.this.categoryRepository.findById(
          CategoryServiceImplTest.this.categoryUpdateRequestWithParent.getCategoryId()))
          .thenReturn(Optional.of(CategoryServiceImplTest.this.categoryUpdate));

      final IllegalStateException exception = assertThrows(IllegalStateException.class,
          () -> CategoryServiceImplTest.this.categoryService.updateCategory(categoryId,
              CategoryServiceImplTest.this.categoryUpdateRequestWithParent));

      assertNotNull(exception);
      assertEquals("Cannot set a child as parent (cycle detected)", exception.getMessage());

      verify(CategoryServiceImplTest.this.categoryRepository)
          .existsByNameAndIdNot(CategoryServiceImplTest.this.categoryUpdateRequest.getName(), categoryId);

      verify(CategoryServiceImplTest.this.categoryRepository)
          .findById(categoryId);

      verify(CategoryServiceImplTest.this.categoryRepository)
          .findById(CategoryServiceImplTest.this.categoryUpdateRequestWithParent.getCategoryId());

      verify(CategoryServiceImplTest.this.categoryRepository, times(0))
          .save(CategoryServiceImplTest.this.categoryUpdate);

    }

    @Test
    @DisplayName("Should update category without updating the parent")
    void test5() {

      final UUID categoryId = UUID.fromString("4f81fd62-f90b-45af-b5ab-ee9ab5f46123");
      when(CategoryServiceImplTest.this.categoryRepository.existsByNameAndIdNot(
          CategoryServiceImplTest.this.categoryUpdateRequest.getName(), categoryId)).thenReturn(false);

      when(CategoryServiceImplTest.this.categoryRepository.findById(
          categoryId)).thenReturn(Optional.of(CategoryServiceImplTest.this.category));

      // when(CategoryServiceImplTest.this.categoryRepository.findById(
      // CategoryServiceImplTest.this.categoryUpdateRequest.getCategoryId()))
      // .thenReturn(Optional.empty());

      when(CategoryServiceImplTest.this.categoryRepository.save(any(Category.class)))
          .thenReturn(CategoryServiceImplTest.this.categoryUpdate);

      final Category result = CategoryServiceImplTest.this.categoryService
          .updateCategory(categoryId, CategoryServiceImplTest.this.categoryUpdateRequest);

      assertNotNull(result);
      assertEquals("updated category", result.getName());

      verify(CategoryServiceImplTest.this.categoryRepository)
          .existsByNameAndIdNot(CategoryServiceImplTest.this.categoryUpdateRequest.getName(), categoryId);

      verify(CategoryServiceImplTest.this.categoryRepository)
          .findById(categoryId);

      verify(CategoryServiceImplTest.this.categoryRepository, times(0))
          .findById(CategoryServiceImplTest.this.categoryUpdateRequest.getCategoryId());

      verify(CategoryServiceImplTest.this.categoryRepository)
          .save(CategoryServiceImplTest.this.categoryUpdate);

    }

    @Test
    @DisplayName("Should update category with updating the parent")
    void test6() {

      final UUID categoryId = UUID.fromString("4f81fd62-f90b-45af-b5ab-ee9ab5f46123");
      when(CategoryServiceImplTest.this.categoryRepository.existsByNameAndIdNot(
          CategoryServiceImplTest.this.categoryUpdateRequestWithParent.getName(), categoryId)).thenReturn(false);

      when(CategoryServiceImplTest.this.categoryRepository.findById(
          categoryId)).thenReturn(Optional.of(CategoryServiceImplTest.this.categoryWithParent));

      when(CategoryServiceImplTest.this.categoryRepository.findById(
          CategoryServiceImplTest.this.categoryUpdateRequestWithParent.getCategoryId()))
          .thenReturn(Optional.of(CategoryServiceImplTest.this.parentUpdateCategory));

      when(CategoryServiceImplTest.this.categoryRepository.save(any(Category.class)))
          .thenReturn(CategoryServiceImplTest.this.categoryUpdateWithParent);

      final Category result = CategoryServiceImplTest.this.categoryService
          .updateCategory(categoryId, CategoryServiceImplTest.this.categoryUpdateRequestWithParent);

      assertNotNull(result);
      assertEquals("updated category", result.getName());
      assertEquals(CategoryServiceImplTest.this.parentUpdateCategory.getId(), result.getParent().getId());

      verify(CategoryServiceImplTest.this.categoryRepository)
          .existsByNameAndIdNot(CategoryServiceImplTest.this.categoryUpdateRequestWithParent.getName(), categoryId);

      verify(CategoryServiceImplTest.this.categoryRepository)
          .findById(categoryId);

      verify(CategoryServiceImplTest.this.categoryRepository)
          .findById(CategoryServiceImplTest.this.categoryUpdateRequestWithParent.getCategoryId());

      verify(CategoryServiceImplTest.this.categoryRepository)
          .save(CategoryServiceImplTest.this.categoryUpdateWithParent);

    }

  }

  @Nested
  @DisplayName("Get all categories Categories")
  class getCategories {

    @Test
    @DisplayName("should return all categories")
    void test1() {

      when(CategoryServiceImplTest.this.categoryRepository.findAll())
          .thenReturn(List.of(CategoryServiceImplTest.this.categoryUpdate, CategoryServiceImplTest.this.category,
              CategoryServiceImplTest.this.parentCategory));

      final List<Category> results = CategoryServiceImplTest.this.categoryService.getAllCategories();

      assertNotNull(results);
      assertEquals(3, results.size());

      verify(CategoryServiceImplTest.this.categoryRepository)
          .findAll();
    }

    @Test
    @DisplayName("should return categories tree")
    void test2() {
      CategoryServiceImplTest.this.categoryWithParent.setId(UUID.fromString("4f81fd62-f90b-45af-b5ab-ee9ab5f46999"));
      CategoryServiceImplTest.this.categoryUpdate
          .setId(UUID.fromString("4f81fd62-f90b-45af-b5ab-ee9ab5f46888"));

      when(CategoryServiceImplTest.this.categoryRepository.findAll())
          .thenReturn(
              List.of(CategoryServiceImplTest.this.parentCategory, CategoryServiceImplTest.this.categoryWithParent,
                  CategoryServiceImplTest.this.categoryUpdate));

      when(CategoryServiceImplTest.this.categoryMapper.toTreeDto(CategoryServiceImplTest.this.categoryWithParent))
          .thenReturn(CategoryServiceImplTest.this.categoryWithParentWithChildren);

      when(CategoryServiceImplTest.this.categoryMapper.toTreeDto(CategoryServiceImplTest.this.parentCategory))
          .thenReturn(CategoryServiceImplTest.this.parentCategoryWithChildren);

      when(CategoryServiceImplTest.this.categoryMapper.toTreeDto(CategoryServiceImplTest.this.categoryUpdate))
          .thenReturn(CategoryServiceImplTest.this.categoryUpdateWithChildren);

      final List<CategoryTreeDto> results = CategoryServiceImplTest.this.categoryService.getCategoryTree();

      assertNotNull(results);
      assertEquals(2, results.size());
      assertTrue(results.stream().anyMatch(c -> c.getChildren().size() >= 1));
      assertTrue(results.stream().anyMatch(c -> c.getChildren().size() == 0));

      verify(CategoryServiceImplTest.this.categoryRepository)
          .findAll();

      verify(CategoryServiceImplTest.this.categoryMapper, times(3))
          .toTreeDto(any(Category.class));
    }

    @Test
    @DisplayName("should return category by id")
    void test3() {

      final UUID categoryId = UUID.fromString("4f81fd62-f90b-45af-b5ab-ee9ab5f46283");
      when(CategoryServiceImplTest.this.categoryRepository.findById(categoryId))
          .thenReturn(Optional.of(CategoryServiceImplTest.this.parentCategory));

      final Category result = CategoryServiceImplTest.this.categoryService.getCategoryById(categoryId);
      assertNotNull(result);
      assertEquals("parent", result.getName());

      verify(CategoryServiceImplTest.this.categoryRepository)
          .findById(categoryId);
    }

    @Test
    @DisplayName("should throw EntityNotFoundException if invalid categoryId")
    void test4() {

      final UUID categoryId = UUID.fromString("4f81fd62-f90b-45af-b5ab-ee9ab5f46283");
      when(CategoryServiceImplTest.this.categoryRepository.findById(categoryId))
          .thenReturn(Optional.empty());

      final EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
          () -> CategoryServiceImplTest.this.categoryService.getCategoryById(categoryId));

      assertNotNull(exception);
      assertEquals("Category not found by id " + categoryId, exception.getMessage());

      verify(CategoryServiceImplTest.this.categoryRepository)
          .findById(categoryId);
    }

    @Test
    @DisplayName("should return category descendants by categoryId")
    void test5() {

      final UUID categoryId = UUID.fromString("4f81fd62-f90b-45af-b5ab-ee9ab5f46111");
      CategoryServiceImplTest.this.categoryWithParent.setId(categoryId);

      when(CategoryServiceImplTest.this.categoryRepository.findAll())
          .thenReturn(
              List.of(CategoryServiceImplTest.this.parentCategory, CategoryServiceImplTest.this.categoryWithParent));

      final Set<UUID> result = CategoryServiceImplTest.this.categoryService.getCategoryDescendantsById(categoryId);

      assertNotNull(result);
      // assertTrue(result.contains(CategoryServiceImplTest.this.parentCategory.getId()));
      assertTrue(result.contains(CategoryServiceImplTest.this.categoryWithParent.getId()));

      verify(CategoryServiceImplTest.this.categoryRepository)
          .findAll();
    }

  }

  @Nested
  @DisplayName("Get all categories Categories")
  class deleteCategories {
    @Test
    @DisplayName("should throw EntityNotFoundException when category doesn't exists")
    void test1() {

      final UUID categoryId = UUID.fromString("4f81fd62-f90b-45af-b5ab-ee9ab5f46123");
      when(CategoryServiceImplTest.this.categoryRepository.existsById(categoryId))
          .thenReturn(false);

      final EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
          () -> CategoryServiceImplTest.this.categoryService.deleteCategory(categoryId));

      assertNotNull(exception);
      assertEquals("Category not found", exception.getMessage());

      verify(CategoryServiceImplTest.this.categoryRepository)
          .existsById(categoryId);

      verify(CategoryServiceImplTest.this.categoryRepository, times(0))
          .existsByParentId(categoryId);

      verify(CategoryServiceImplTest.this.itemRepository, times(0))
          .existsByCategory_Id(categoryId);

      verify(CategoryServiceImplTest.this.categoryRepository, times(0))
          .deleteById(categoryId);
    }

    @Test
    @DisplayName("should throw ConflictException if category is a parent of another category")
    void test2() {

      final UUID categoryId = UUID.fromString("4f81fd62-f90b-45af-b5ab-ee9ab5f46123");
      when(CategoryServiceImplTest.this.categoryRepository.existsById(categoryId))
          .thenReturn(true);

      when(CategoryServiceImplTest.this.categoryRepository.existsByParentId(categoryId))
          .thenReturn(true);

      final ConflictException exception = assertThrows(ConflictException.class,
          () -> CategoryServiceImplTest.this.categoryService.deleteCategory(categoryId));

      assertNotNull(exception);
      assertEquals("Category can not be deleted cause of children categories", exception.getMessage());

      verify(CategoryServiceImplTest.this.categoryRepository)
          .existsById(categoryId);

      verify(CategoryServiceImplTest.this.categoryRepository)
          .existsByParentId(categoryId);

      verify(CategoryServiceImplTest.this.itemRepository, times(0))
          .existsByCategory_Id(categoryId);

      verify(CategoryServiceImplTest.this.categoryRepository, times(0))
          .deleteById(categoryId);
    }

    @Test
    @DisplayName("should throw ConflictException if category is a parent of another category")
    void test3() {

      final UUID categoryId = UUID.fromString("4f81fd62-f90b-45af-b5ab-ee9ab5f46123");
      when(CategoryServiceImplTest.this.categoryRepository.existsById(categoryId))
          .thenReturn(true);

      when(CategoryServiceImplTest.this.categoryRepository.existsByParentId(categoryId))
          .thenReturn(false);

      when(CategoryServiceImplTest.this.itemRepository.existsByCategory_Id(categoryId))
          .thenReturn(true);

      final ConflictException exception = assertThrows(ConflictException.class,
          () -> CategoryServiceImplTest.this.categoryService.deleteCategory(categoryId));

      assertNotNull(exception);
      assertEquals("Category can not be deleted cause of existing items in category", exception.getMessage());

      verify(CategoryServiceImplTest.this.categoryRepository)
          .existsById(categoryId);

      verify(CategoryServiceImplTest.this.categoryRepository)
          .existsByParentId(categoryId);

      verify(CategoryServiceImplTest.this.itemRepository)
          .existsByCategory_Id(categoryId);

      verify(CategoryServiceImplTest.this.categoryRepository, times(0))
          .deleteById(categoryId);
    }

    @Test
    @DisplayName("should delete category")
    void test4() {

      final UUID categoryId = UUID.fromString("4f81fd62-f90b-45af-b5ab-ee9ab5f46123");
      when(CategoryServiceImplTest.this.categoryRepository.existsById(categoryId))
          .thenReturn(true);

      when(CategoryServiceImplTest.this.categoryRepository.existsByParentId(categoryId))
          .thenReturn(false);

      when(CategoryServiceImplTest.this.itemRepository.existsByCategory_Id(categoryId))
          .thenReturn(false);

      CategoryServiceImplTest.this.categoryService.deleteCategory(categoryId);

      verify(CategoryServiceImplTest.this.categoryRepository)
          .existsById(categoryId);

      verify(CategoryServiceImplTest.this.categoryRepository)
          .existsByParentId(categoryId);

      verify(CategoryServiceImplTest.this.itemRepository)
          .existsByCategory_Id(categoryId);

      verify(CategoryServiceImplTest.this.categoryRepository)
          .deleteById(categoryId);
    }

    @Test
    @DisplayName("should throw EntityNotFoundException when categoryId is null")
    void test5() {

      final UUID categoryId = null;
      when(CategoryServiceImplTest.this.categoryRepository.existsById(categoryId))
          .thenReturn(false);

      final EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
          () -> CategoryServiceImplTest.this.categoryService.deleteCategory(categoryId));

      assertNotNull(exception);
      assertEquals("Category not found", exception.getMessage());

      verify(CategoryServiceImplTest.this.categoryRepository)
          .existsById(categoryId);

      verify(CategoryServiceImplTest.this.categoryRepository, times(0))
          .existsByParentId(categoryId);

      verify(CategoryServiceImplTest.this.itemRepository, times(0))
          .existsByCategory_Id(categoryId);

      verify(CategoryServiceImplTest.this.categoryRepository, times(0))
          .deleteById(categoryId);
    }

  }

}
