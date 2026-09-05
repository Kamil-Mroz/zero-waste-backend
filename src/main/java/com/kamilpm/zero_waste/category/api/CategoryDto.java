package com.kamilpm.zero_waste.category.api;

import java.util.UUID;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoryDto {
  private UUID id;
  private String name;
  private UUID parentId;

}
