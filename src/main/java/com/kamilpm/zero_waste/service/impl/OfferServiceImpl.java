package com.kamilpm.zero_waste.service.impl;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import com.kamilpm.zero_waste.domain.entity.Item;
import com.kamilpm.zero_waste.domain.entity.ItemState;
import com.kamilpm.zero_waste.domain.entity.Offer;
import com.kamilpm.zero_waste.domain.entity.OfferStatus;
import com.kamilpm.zero_waste.exception.ForbiddenException;
import com.kamilpm.zero_waste.repository.ItemRepository;
import com.kamilpm.zero_waste.repository.OfferRepository;
import com.kamilpm.zero_waste.security.MyUserDetails;
import com.kamilpm.zero_waste.service.AuthService;
import com.kamilpm.zero_waste.service.OfferService;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OfferServiceImpl implements OfferService {
  private final SimpMessagingTemplate messagingTemplate;
  private final OfferRepository offerRepository;
  private final ItemRepository itemRepository;
  private final AuthService authService;

  @Transactional
  public Offer acceptOffer(UUID id) {
    MyUserDetails user = authService.getRequiredAuthenticatedUserDetails();
    Offer offer = offerRepository.findById(id).orElseThrow();
    Item item = itemRepository.findByIdForUpdate(offer.getItem().getId()).orElseThrow();
    if (Objects.equals(item.getOwner().getId(), user.getId())
        || Objects.equals(offer.getBuyer().getId(), user.getId())) {
      throw new ForbiddenException("You cannot accept your own offer");
    }
    item.setState(ItemState.GIVEN);
    offer.setStatus(OfferStatus.ACCEPTED);

    List<Offer> rejectedOffers = offerRepository.findByItem_idAndIdNot(item.getId(), offer.getId());
    for (Offer rejectedOffer : rejectedOffers) {
      rejectedOffer.setStatus(OfferStatus.REJECTED);
    }

    itemRepository.save(item);
    offerRepository.save(offer);
    offerRepository.saveAll(rejectedOffers);

    messagingTemplate.convertAndSendToUser(offer.getBuyer().getEmail(), "/queue/offers/", "ACCEPTED");
    for (Offer rejectedOffer : rejectedOffers) {
      messagingTemplate.convertAndSendToUser(rejectedOffer.getBuyer().getEmail(), "/queue/offers/", "REJECTED");
    }
    return offer;
  }

}
