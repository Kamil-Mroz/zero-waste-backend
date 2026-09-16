package com.kamilpm.zero_waste.offer.mapper;

import org.springframework.stereotype.Component;

import com.kamilpm.zero_waste.common.dto.SimpleItemData;
import com.kamilpm.zero_waste.common.dto.SimpleOfferData;
import com.kamilpm.zero_waste.common.dto.UserSummaryDto;
import com.kamilpm.zero_waste.common.dto.UserSummaryWithEmailDto;
import com.kamilpm.zero_waste.offer.dto.OfferDto;
import com.kamilpm.zero_waste.offer.dto.OfferWithEmailDto;
import com.kamilpm.zero_waste.offer.entity.Offer;

@Component
public class OfferMapper {

  public OfferDto toDto(Offer offer, SimpleItemData item, UserSummaryDto buyer) {
    if (offer == null) {
      return null;
    }
    return new OfferDto(offer.getId(), item, buyer, offer.getStatus());

  }

  public OfferWithEmailDto toWithEmailDto(Offer offer, SimpleItemData item, UserSummaryWithEmailDto buyer) {
    if (offer == null) {
      return null;
    }
    return new OfferWithEmailDto(offer.getId(), item, buyer, offer.getStatus());
  };

  public SimpleOfferData toSimpleDto(Offer offer) {
    if (offer == null)
      return null;

    return new SimpleOfferData(offer.getId(), offer.getItemId(), offer.getBuyerId(), offer.getStatus());

  }

}
