package com.kamilpm.zero_waste.item.api;

import java.util.Collection;
import java.util.UUID;

public record DeleteItemsEvent(Collection<UUID> itemIds) {

}
