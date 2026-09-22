package com.kamilpm.zero_waste.common.events;

import java.util.List;
import java.util.UUID;

public record DeleteUsersEvent(List<UUID> ids) {

}
