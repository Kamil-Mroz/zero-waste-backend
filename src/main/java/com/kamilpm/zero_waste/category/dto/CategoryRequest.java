package com.kamilpm.zero_waste.category.dto;

import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record CategoryRequest(
    UUID categoryId,
    @NotBlank(message = "Category name must be provide") @Pattern(regexp = "^[\\p{L}]+(?: [\\p{L}]+)*$", message = "Category name can contain only letters") String name) {
}
