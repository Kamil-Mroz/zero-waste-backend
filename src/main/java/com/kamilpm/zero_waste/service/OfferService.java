package com.kamilpm.zero_waste.service;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.kamilpm.zero_waste.domain.dto.OfferDto;
import com.kamilpm.zero_waste.domain.entity.Offer;
import com.kamilpm.zero_waste.domain.entity.OfferStatus;

public interface OfferService {
  void makeOffer(UUID id);

  Offer getOfferById(UUID id);

  void acceptOffer(UUID id);

  void rejectOffer(UUID id);

  void cancelOffer(UUID id);

  Page<OfferDto> getMyOffers(Pageable pageable, OfferStatus status);

  Page<OfferDto> getReceivedOffers(Pageable pageable, OfferStatus status);

  void deleteAllByUserIds(List<UUID> ids);
}
