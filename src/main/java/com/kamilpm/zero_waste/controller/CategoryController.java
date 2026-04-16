package com.kamilpm.zero_waste.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kamilpm.zero_waste.domain.dto.CategoryDto;
import com.kamilpm.zero_waste.domain.dto.CategoryTreeDto;
import com.kamilpm.zero_waste.domain.entity.Category;
import com.kamilpm.zero_waste.domain.mapper.CategoryMapper;
import com.kamilpm.zero_waste.domain.request.CategoryRequest;
import com.kamilpm.zero_waste.service.CategoryService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PathVariable;

@RestController
@RequestMapping(path = "/api/v{version}/categories", version = "1")
@RequiredArgsConstructor
public class CategoryController {

  private final CategoryService categoryService;
  private final CategoryMapper categoryMapper;

  @GetMapping
  public ResponseEntity<List<CategoryDto>> getAllCategories() {

    List<CategoryDto> categories = categoryService.getAllCategories()
        .stream().map(categoryMapper::toDto).toList();
    return ResponseEntity.ok(categories);
  }

  @GetMapping("/{id}")
  public ResponseEntity<CategoryDto> getCategory(@PathVariable UUID id) {

    Category category = categoryService.getCategoryById(id);
    return ResponseEntity.ok(categoryMapper.toDto(category));
  }

  @PostMapping
  public ResponseEntity<CategoryDto> createCategory(@Valid @RequestBody CategoryRequest categoryRequest) {

    Category category = categoryService.createCategory(categoryRequest);

    return new ResponseEntity<>(categoryMapper.toDto(category), HttpStatus.CREATED);
  }

  @GetMapping("/tree")
  public ResponseEntity<List<CategoryTreeDto>> getCategoriesTree() {
    List<CategoryTreeDto> categories = categoryService.getCategoryTree();

    return ResponseEntity.ok(categories);
  }

  @PutMapping("/{id}")
  public ResponseEntity<CategoryDto> updateCategory(@PathVariable UUID id,
      @Valid @RequestBody CategoryRequest categoryRequest) {
    Category category = categoryService.updateCategory(id, categoryRequest);

    return ResponseEntity.ok(categoryMapper.toDto(category));
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteCategory(@PathVariable UUID id) {
    categoryService.deleteCategory(id);

    return ResponseEntity.noContent().build();
  }

}
