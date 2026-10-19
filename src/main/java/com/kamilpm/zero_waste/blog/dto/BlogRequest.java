package com.kamilpm.zero_waste.blog.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record BlogRequest(
    @NotBlank @Size(max = 255, message = "Title can not exceed 255 characters") String title,
    @NotBlank @Size(max = 255, message = "Description can not exceed 255 characters") String description,
    @NotBlank String content) {
}
