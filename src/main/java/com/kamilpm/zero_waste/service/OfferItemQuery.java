package com.kamilpm.zero_waste.service;

import com.kamilpm.zero_waste.domain.entity.Item;

public interface OfferItemQuery {

  void deleteAllByItem(Item item);
}
