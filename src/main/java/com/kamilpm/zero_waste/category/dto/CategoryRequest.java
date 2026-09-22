package com.kamilpm.zero_waste.category.dto;

import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoryRequest {
  private UUID categoryId;

  @NotBlank(message = "Category name must be provide")
  @Pattern(regexp = "^[\\p{L}]+(?: [\\p{L}]+)*$", message = "Category name can contain only letters")
  private String name;
}
