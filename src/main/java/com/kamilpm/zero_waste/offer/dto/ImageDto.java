package com.kamilpm.zero_waste.offer.dto;

import java.util.UUID;

public record ImageDto(

    UUID id,
    String originalName,
    String url) {
}
