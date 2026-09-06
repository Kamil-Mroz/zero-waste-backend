package com.kamilpm.zero_waste.auth.api;

import java.util.List;
import java.util.UUID;

public record RevokeRefreshTokenEvent(List<UUID> ids) {

}
