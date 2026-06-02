package com.kamilpm.zero_waste.domain.mapper;

import com.kamilpm.zero_waste.domain.dto.OfferDto;
import com.kamilpm.zero_waste.domain.entity.Offer;

public interface OfferMapper {
  OfferDto toDto(Offer offer);

}
