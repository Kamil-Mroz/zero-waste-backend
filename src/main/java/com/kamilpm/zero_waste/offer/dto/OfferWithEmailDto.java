package com.kamilpm.zero_waste.offer.dto;

import java.util.UUID;

import com.kamilpm.zero_waste.common.dto.OfferStatus;
import com.kamilpm.zero_waste.common.dto.SimpleItemData;
import com.kamilpm.zero_waste.common.dto.UserSummaryWithEmailDto;

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
  private SimpleItemData item;
  private UserSummaryWithEmailDto buyer;
  private OfferStatus status;
}
