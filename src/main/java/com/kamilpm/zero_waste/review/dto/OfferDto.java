package com.kamilpm.zero_waste.review.dto;

import java.util.UUID;

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
