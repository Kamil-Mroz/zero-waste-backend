package com.kamilpm.zero_waste.category.dto;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public record CategoryTreeDto(
    UUID id,
    String name,
    List<CategoryTreeDto> children) {
  public CategoryTreeDto {
    children = children == null ? new ArrayList<>() : new ArrayList<>(children);
  }
}
