package com.kamilpm.zero_waste.offer.api;

import java.util.UUID;

import com.kamilpm.zero_waste.common.dto.UserSummaryDto;
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
public class OfferDto {
  private UUID id;
  private ItemDto item;
  private UserSummaryDto buyer;
  private OfferStatus status;

}
