package com.kamilpm.zero_waste.user.api;

import java.util.List;
import java.util.UUID;

public record UsersDeletedEvent(List<UUID> ids) {

}
