package com.kamilpm.zero_waste.common.events;

import java.util.Collection;
import java.util.UUID;

public record UserReadyToDeleteEvent(Collection<UUID> ids) {

}
