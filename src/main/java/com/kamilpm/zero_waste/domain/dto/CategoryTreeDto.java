package com.kamilpm.zero_waste.domain.dto;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoryTreeDto {
  private UUID id;
  private String name;
  private final List<CategoryTreeDto> children = new ArrayList<>();

}
