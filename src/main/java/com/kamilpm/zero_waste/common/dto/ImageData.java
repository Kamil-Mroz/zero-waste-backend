package com.kamilpm.zero_waste.common.dto;

import java.util.UUID;

public record ImageData(UUID id,
    String originalName,
    String url) {
}
