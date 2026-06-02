package com.kamilpm.zero_waste.domain.request;

import java.time.Instant;
import java.util.UUID;

public record CursorRequest(Instant createdAt, UUID id) {

}
