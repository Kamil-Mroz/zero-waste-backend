package com.kamilpm.zero_waste.review.dto;

import java.util.UUID;

import com.kamilpm.zero_waste.common.dto.OfferStatus;
import com.kamilpm.zero_waste.common.dto.SimpleItemData;
import com.kamilpm.zero_waste.common.dto.UserSummaryDto;

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
  private SimpleItemData item;
  private UserSummaryDto buyer;
  private OfferStatus status;

}
