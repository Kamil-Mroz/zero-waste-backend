package com.kamilpm.zero_waste.common.interfaces;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import com.kamilpm.zero_waste.common.dto.CategoryData;

public interface CategoryProvider {

  public void existsByCategoryId(UUID id);

  public CategoryData getCategoryById(UUID id);

  public Set<UUID> getCategoryDescendantsById(UUID categoryId);

  public Map<UUID, CategoryData> getCategoriesByIds(Collection<UUID> categoryIds);

}
