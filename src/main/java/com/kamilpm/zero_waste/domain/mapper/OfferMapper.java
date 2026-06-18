package com.kamilpm.zero_waste.domain.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.kamilpm.zero_waste.domain.dto.OfferDto;
import com.kamilpm.zero_waste.domain.entity.Offer;

@Mapper(componentModel = "spring", uses = { UserMapper.class, ItemMapper.class })
public interface OfferMapper {

  @Mapping(target="item", source="item", qualifiedByName = "itemWithOwner")
  OfferDto toDto(Offer offer);

}
