package com.kamilpm.zero_waste.offer.dto;

import java.util.UUID;

import com.kamilpm.zero_waste.common.dto.UserSummaryWithEmailDto;
import com.kamilpm.zero_waste.item.api.ItemDto;
import com.kamilpm.zero_waste.offer.entity.OfferStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OfferWithEmailDto {
  private UUID id;
  private ItemDto item;
  private UserSummaryWithEmailDto buyer;
  private OfferStatus status;
}
