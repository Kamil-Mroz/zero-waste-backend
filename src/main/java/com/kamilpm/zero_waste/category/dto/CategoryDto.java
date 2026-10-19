package com.kamilpm.zero_waste.category.dto;

import java.util.UUID;

public record CategoryDto(
    UUID id,
    String name,
    UUID parentId) {
}
