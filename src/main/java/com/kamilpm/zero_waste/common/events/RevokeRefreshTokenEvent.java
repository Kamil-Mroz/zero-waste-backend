package com.kamilpm.zero_waste.common.events;

import java.util.List;
import java.util.UUID;

public record RevokeRefreshTokenEvent(List<UUID> ids) {

}
