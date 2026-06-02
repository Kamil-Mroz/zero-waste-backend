package com.kamilpm.zero_waste.domain.response;

import java.time.Instant;
import java.util.UUID;

public record PublicUserProfileResponse(
    UUID id,
    String firstName,
    String lastName,
    Instant joinedAt,
    int itemCount

) {

}
