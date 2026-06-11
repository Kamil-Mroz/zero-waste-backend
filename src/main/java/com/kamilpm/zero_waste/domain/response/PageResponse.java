package com.kamilpm.zero_waste.domain.response;

import java.util.List;

import lombok.Builder;

@Builder
public record PageResponse<T>(
    List<T> content,
    int page,
    int size,
    long totalElements,
    int totalPages) {
}
