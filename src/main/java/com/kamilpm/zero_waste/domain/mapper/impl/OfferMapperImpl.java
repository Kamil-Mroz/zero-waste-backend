package com.kamilpm.zero_waste.domain.mapper.impl;

import org.springframework.stereotype.Component;

import com.kamilpm.zero_waste.domain.dto.OfferDto;
import com.kamilpm.zero_waste.domain.entity.Offer;
import com.kamilpm.zero_waste.domain.mapper.ItemMapper;
import com.kamilpm.zero_waste.domain.mapper.OfferMapper;
import com.kamilpm.zero_waste.domain.mapper.UserMapper;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class OfferMapperImpl implements OfferMapper {

  private final UserMapper userMapper;
  private final ItemMapper itemMapper;

  @Override
  public OfferDto toDto(Offer offer) {
    return OfferDto
        .builder()
        .id(offer.getId())
        .item(itemMapper.toDtoWithOwner(offer.getItem()))
        .buyer(userMapper.toDto(offer.getBuyer()))
        .status(offer.getStatus())
        .build();
  }

}
