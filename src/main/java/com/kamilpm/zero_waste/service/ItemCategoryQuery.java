package com.kamilpm.zero_waste.service;

import java.util.UUID;

public interface ItemCategoryQuery {
  boolean existsByCategory_Id(UUID id);

}
