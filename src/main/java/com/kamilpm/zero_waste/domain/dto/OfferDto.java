package com.kamilpm.zero_waste.domain.dto;

import java.util.UUID;


import com.kamilpm.zero_waste.domain.entity.OfferStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OfferDto {
  private UUID id;
  private ItemDto item;
  private UserDto buyer;
  private OfferStatus status;
}
