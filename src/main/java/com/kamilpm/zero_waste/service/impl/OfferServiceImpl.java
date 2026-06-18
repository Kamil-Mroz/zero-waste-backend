package com.kamilpm.zero_waste.service.impl;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.kamilpm.zero_waste.domain.dto.OfferDto;
import com.kamilpm.zero_waste.domain.entity.Item;
import com.kamilpm.zero_waste.domain.entity.ItemState;
import com.kamilpm.zero_waste.domain.entity.NotificationType;
import com.kamilpm.zero_waste.domain.entity.Offer;
import com.kamilpm.zero_waste.domain.entity.OfferStatus;
import com.kamilpm.zero_waste.domain.entity.User;
import com.kamilpm.zero_waste.domain.mapper.OfferMapper;
import com.kamilpm.zero_waste.exception.ConflictException;
import com.kamilpm.zero_waste.exception.ForbiddenException;
import com.kamilpm.zero_waste.repository.OfferRepository;
import com.kamilpm.zero_waste.security.MyUserDetails;
import com.kamilpm.zero_waste.service.AuthService;
import com.kamilpm.zero_waste.service.ItemService;
import com.kamilpm.zero_waste.service.NotificationService;
import com.kamilpm.zero_waste.service.OfferService;

import jakarta.persistence.EntityNotFoundException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OfferServiceImpl implements OfferService {
  private final OfferRepository offerRepository;
  private final ItemService itemService;
  private final AuthService authService;
  private final NotificationService notificationService;
  private final OfferMapper offerMapper;

  @Override
  public Offer getOfferById(UUID id) {
    Offer offer = offerRepository.findDetailsById(id).orElseThrow(() -> new EntityNotFoundException("Offer not found"));
    return offer;
  }

  private void ensurePending(Offer offer) {
    if (offer.getStatus() != OfferStatus.PENDING)
      throw new ConflictException("Offer is no longer pending");
  }

  @Transactional(propagation = Propagation.REQUIRES_NEW)
  @Override
  public void acceptOffer(UUID id) {
    MyUserDetails user = authService.getRequiredAuthenticatedUserDetails();
    Offer offer = getOfferById(id);
    ensurePending(offer);
    Item item = itemService.findByIdForUpdate(offer.getItem().getId());
    if (!Objects.equals(item.getOwner().getId(), user.getId())) {
      throw new ForbiddenException("You cannot accept your own offer");
    }
    item.setState(ItemState.GIVEN);
    offer.setStatus(OfferStatus.ACCEPTED);

    List<Offer> rejectedOffers = offerRepository.findByItem_idAndStatusAndIdNot(item.getId(), OfferStatus.PENDING,
        offer.getId());
    for (Offer rejectedOffer : rejectedOffers) {
      rejectedOffer.setStatus(OfferStatus.REJECTED);
    }

    itemService.saveItem(item);
    offerRepository.save(offer);
    offerRepository.saveAll(rejectedOffers);

    notificationService.sendNotification(offer.getBuyer(), NotificationType.OFFER_ACCEPTED, "Offer accepted",
        "Your request was accepted.", offer.getId(), "OFFER");
    for (Offer rejectedOffer : rejectedOffers) {
      notificationService.sendNotification(rejectedOffer.getBuyer(), NotificationType.OFFER_REJECTED, "Offer rejected",
          "Your request was declined.", offer.getId(), "OFFER");
    }
  }

  @Transactional
  @Override
  public void rejectOffer(UUID id) {
    MyUserDetails user = authService.getRequiredAuthenticatedUserDetails();

    Offer offer = getOfferById(id);
    if (!Objects.equals(offer.getItem().getOwner().getId(), user.getId()))
      throw new ForbiddenException("Unable to reject an offer that you are not the owner of item");
    ensurePending(offer);

    offer.setStatus(OfferStatus.REJECTED);
    offerRepository.save(offer);

    notificationService.sendNotification(offer.getBuyer(), NotificationType.OFFER_REJECTED, "Offer rejected",
        "Your request was declined.", offer.getId(), "OFFER");
  }

  @Transactional
  @Override
  public void makeOffer(UUID id) {
    User user = authService.getRequiredAuthenticatedUserEntity();

    Item item = itemService.findByIdForUpdate(id);
    if (Objects.equals(user.getId(), item.getOwner().getId()))
      throw new ConflictException("You can not make an offer on your own item");

    if (ItemState.GIVEN == item.getState())
      throw new ConflictException("Item already given");
    if (offerRepository.existsByBuyer_IdAndItem_Id(user.getId(), id))
      throw new ConflictException("You have made already an offer for this item");

    Offer offer = Offer.builder()
        .buyer(user)
        .item(item)
        .status(OfferStatus.PENDING)
        .build();
    offerRepository.save(offer);
    notificationService.sendNotification(
        item.getOwner(),
        NotificationType.OFFER_RECEIVED,
        "New interest request",
        user.getFirstName() + " " + user.getLastName() + " is interested in your item.",
        offer.getId(),
        "OFFER");
  }

  @Transactional
  @Override
  public void cancelOffer(UUID id) {
    MyUserDetails user = authService.getRequiredAuthenticatedUserDetails();

    Offer offer = getOfferById(id);

    if (!Objects.equals(user.getId(), offer.getBuyer().getId()))
      throw new ForbiddenException("Can not cancel this offer");

    ensurePending(offer);

    offer.setStatus(OfferStatus.CANCELLED);
    offerRepository.save(offer);
    notificationService.sendNotification(
        offer.getItem().getOwner(),
        NotificationType.OFFER_CANCELLED,
        "Offer cancelled",
        "Offer for the item (" + offer.getItem().getTitle() + ") was cancelled.",
        offer.getId(),
        "OFFER");
  }

  @Override
  @Transactional
  public Page<OfferDto> getMyOffers(Pageable pageable, OfferStatus status) {

    MyUserDetails user = authService.getRequiredAuthenticatedUserDetails();
    if (status != null) {
      return offerRepository.findByBuyer_IdAndStatus(user.getId(), status, pageable).map(offerMapper::toDto);
    }
    return offerRepository.findByBuyer_Id(user.getId(), pageable).map(offerMapper::toDto);

  }

  @Override
  @Transactional
  public Page<OfferDto> getReceivedOffers(Pageable pageable, OfferStatus status) {
    MyUserDetails user = authService.getRequiredAuthenticatedUserDetails();
    if (status != null) {
      return offerRepository.findByItem_Owner_IdAndStatus(user.getId(), status, pageable).map(offerMapper::toDto);
    }
    return offerRepository.findByItem_Owner_Id(user.getId(), pageable).map(offerMapper::toDto);

  }

  @Override
  public void deleteAllByUserIds(List<UUID> ids) {
    offerRepository.deleteByBuyer_IdIn(ids);
    offerRepository.deleteByItem_Owner_IdIn(ids);

  }
}
