package com.kamilpm.zero_waste.review.dto;

import java.util.UUID;

public record ImageDto(

    UUID id,
    String originalName,
    String url) {
}
