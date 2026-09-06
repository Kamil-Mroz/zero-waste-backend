package com.kamilpm.zero_waste.offer.api;

import java.util.Collection;
import java.util.UUID;

public record DeleteOffersEvent(Collection<UUID> offerIds) {

}
