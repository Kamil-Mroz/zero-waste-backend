package com.kamilpm.zero_waste.domain.request;

import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
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
  private String name;
}
