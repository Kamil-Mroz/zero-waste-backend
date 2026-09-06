package com.kamilpm.zero_waste.common.dto;

import java.time.Instant;
import java.util.UUID;

public record CursorRequest(Instant createdAt, UUID id) {

}
