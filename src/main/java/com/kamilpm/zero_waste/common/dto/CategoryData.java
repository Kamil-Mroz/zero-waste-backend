package com.kamilpm.zero_waste.common.dto;

import java.util.UUID;

public record CategoryData(UUID id,
    String name,
    UUID parentId) {
}
