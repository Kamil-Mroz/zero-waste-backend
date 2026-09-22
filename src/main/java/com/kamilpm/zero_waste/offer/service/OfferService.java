package com.kamilpm.zero_waste.offer.service;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import com.kamilpm.zero_waste.common.dto.CurrentUser;
import com.kamilpm.zero_waste.common.dto.ItemState;
import com.kamilpm.zero_waste.common.dto.NotificationRecipient;
import com.kamilpm.zero_waste.common.dto.NotificationReferenceType;
import com.kamilpm.zero_waste.common.dto.NotificationType;
import com.kamilpm.zero_waste.common.dto.OfferStatus;
import com.kamilpm.zero_waste.common.dto.SimpleItemData;
import com.kamilpm.zero_waste.common.dto.UserSummaryWithEmailDto;
import com.kamilpm.zero_waste.common.dto.UserVisibility;
import com.kamilpm.zero_waste.common.entity.ModerationStatus;
import com.kamilpm.zero_waste.common.events.BanEvent;
import com.kamilpm.zero_waste.common.events.OfferAcceptEvent;
import com.kamilpm.zero_waste.common.events.SendNotificationEvent;
import com.kamilpm.zero_waste.common.events.SendNotificationsEvent;
import com.kamilpm.zero_waste.common.events.UnbanEvent;
import com.kamilpm.zero_waste.common.exception.ConflictException;
import com.kamilpm.zero_waste.common.exception.EntityNotFoundException;
import com.kamilpm.zero_waste.common.exception.ForbiddenException;
import com.kamilpm.zero_waste.common.interfaces.CurrentUserProvider;
import com.kamilpm.zero_waste.common.interfaces.ItemProvider;
import com.kamilpm.zero_waste.common.interfaces.UserProvider;
import com.kamilpm.zero_waste.offer.dto.OfferDto;
import com.kamilpm.zero_waste.offer.dto.OfferWithEmailDto;
import com.kamilpm.zero_waste.offer.entity.Offer;
import com.kamilpm.zero_waste.offer.mapper.OfferMapper;
import com.kamilpm.zero_waste.offer.repository.OfferRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OfferService {
  private final OfferRepository offerRepository;
  private final OfferMapper offerMapper;
  private final ItemProvider itemOfferApi;
  private final CurrentUserProvider currentUser;
  private final UserProvider userOfferApi;
  private final ApplicationEventPublisher events;

  private Offer getOfferById(UUID id) {
    Offer offer = offerRepository.findDetailsById(id).orElseThrow(() -> new EntityNotFoundException("Offer not found"));
    return offer;
  }

  private void ensurePending(Offer offer) {
    if (offer.getStatus() != OfferStatus.PENDING)
      throw new ConflictException("Offer is no longer pending");
  }

  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void acceptOffer(UUID id) {
    CurrentUser user = currentUser.getRequiredAuthenticatedUser();
    Offer offer = getOfferById(id);
    ensurePending(offer);

    SimpleItemData item = itemOfferApi.findByIdForUpdate(offer.getItemId());

    if (!Objects.equals(item.ownerId(), user.id())) {
      throw new ForbiddenException("You cannot accept your own offer");
    }
    if (!Objects.equals(item.ownerId(), user.id())) {
      throw new ForbiddenException("Cannot accept offer on an item that you do not own");
    }
    if (item.state() != ItemState.AVAILABLE || item.moderationStatus() != ModerationStatus.VISIBLE) {
      throw new ForbiddenException("Cannot accept offer on non available item");
    }

    offer.setStatus(OfferStatus.ACCEPTED);

    List<Offer> rejectedOffers = offerRepository.findByItemIdAndStatusAndIdNot(item.id(), OfferStatus.PENDING,
        offer.getId());
    for (Offer rejectedOffer : rejectedOffers) {
      rejectedOffer.setStatus(OfferStatus.REJECTED);
    }

    String buyerEmail = userOfferApi.getUserEmail(offer.getBuyerId());

    offerRepository.save(offer);
    offerRepository.saveAll(rejectedOffers);

    events
        .publishEvent(
            new OfferAcceptEvent(offer.getItemId()));

    events
        .publishEvent(
            new SendNotificationEvent(offer.getBuyerId(), buyerEmail,
                NotificationType.OFFER_ACCEPTED, "Offer accepted",
                "Your request was accepted.", offer.getId(),
                NotificationReferenceType.OFFER));

    List<NotificationRecipient> rejectedBuyers = userOfferApi
        .getUsersEmail(rejectedOffers.stream().map(o -> o.getBuyerId()).toList());

    events.publishEvent(
        new SendNotificationsEvent(rejectedBuyers, NotificationType.OFFER_REJECTED,
            "Offer rejected",
            "Your request was declined.", offer.getId(),
            NotificationReferenceType.OFFER));
  }

  @Transactional
  public void rejectOffer(UUID id) {
    CurrentUser user = currentUser.getRequiredAuthenticatedUser();

    Offer offer = getOfferById(id);
    UUID buyerId = offer.getBuyerId();
    SimpleItemData item = itemOfferApi.findById(offer.getItemId());
    if (!Objects.equals(item.ownerId(), user.id()))
      throw new ForbiddenException("Unable to reject an offer that you are not the owner of item");
    ensurePending(offer);

    String buyerEmail = userOfferApi.getUserEmail(buyerId);

    offer.setStatus(OfferStatus.REJECTED);
    offerRepository.save(offer);

    events.publishEvent(
        new SendNotificationEvent(buyerId, buyerEmail,
            NotificationType.OFFER_REJECTED, "Offer rejected",
            "Your request was declined.", offer.getId(),
            NotificationReferenceType.OFFER));

  }

  @Transactional
  public void makeOffer(UUID id) {
    CurrentUser user = currentUser.getRequiredAuthenticatedUser();

    SimpleItemData item = itemOfferApi.findByIdForUpdate(id);

    if (Objects.equals(user.id(), item.ownerId()))
      throw new ConflictException("You can not make an offer on your own item");

    if (userOfferApi.isUserDemo(item.ownerId()))
      throw new ForbiddenException("Unable to interact with demo users");

    if (ItemState.AVAILABLE != item.state())
      throw new ForbiddenException("Unable to make an offer to an unavailable item");

    if (offerRepository.existsByBuyerIdAndItemId(user.id(), id))
      throw new ConflictException("You have made already an offer for this item");

    if (item.moderationStatus() != ModerationStatus.VISIBLE) {
      throw new ForbiddenException("Unable to make an offer for a hidden item");

    }

    Offer offer = Offer.builder()
        .buyerId(user.id())
        .itemId(item.id())
        .buyerVisibility(UserVisibility.VISIBLE)
        .status(OfferStatus.PENDING)
        .build();
    offerRepository.save(offer);

    String itemOwnerEmail = userOfferApi.getUserEmail(item.ownerId());

    events.publishEvent(
        new SendNotificationEvent(item.ownerId(), itemOwnerEmail,
            NotificationType.OFFER_RECEIVED,
            "New interest request",
            user.nickname() + " is interested in you item",
            offer.getId(),
            NotificationReferenceType.OFFER)

    );
  }

  @Transactional
  public void cancelOffer(UUID id) {
    CurrentUser user = currentUser.getRequiredAuthenticatedUser();

    Offer offer = getOfferById(id);

    SimpleItemData item = itemOfferApi.findByIdForUpdate(offer.getItemId());
    String itemOwnerEmail = userOfferApi.getUserEmail(item.ownerId());

    if (!Objects.equals(user.id(), offer.getBuyerId()))
      throw new ForbiddenException("Cannot cancel others offers");

    ensurePending(offer);

    offer.setStatus(OfferStatus.CANCELLED);
    offerRepository.save(offer);

    events.publishEvent(

        new SendNotificationEvent(item.ownerId(), itemOwnerEmail,
            NotificationType.OFFER_CANCELLED,
            "Offer cancelled",
            "Offer for the item (" + item.title() + ") was cancelled.",
            offer.getId(),
            NotificationReferenceType.OFFER));
  }

  @Transactional
  public Page<OfferDto> getMyOffers(Pageable pageable, OfferStatus status) {

    CurrentUser user = currentUser.getRequiredAuthenticatedUser();
    Page<Offer> offers = status != null ? offerRepository.findByBuyerIdAndStatus(user.id(), status, pageable)
        : offerRepository.findByBuyerId(user.id(), pageable);

    Set<UUID> itemIds = offers.getContent().stream().map(offer -> offer.getItemId()).collect(Collectors.toSet());
    Map<UUID, SimpleItemData> itemsById = itemOfferApi.getItemsByIds(itemIds);

    return offers.map(offer -> offerMapper.toDto(offer, itemsById.get(offer.getItemId()),
        null));

  }

  @Transactional
  public Page<OfferWithEmailDto> getReceivedOffers(Pageable pageable, OfferStatus status) {
    CurrentUser user = currentUser.getRequiredAuthenticatedUser();
    Map<UUID, SimpleItemData> itemsById = itemOfferApi.getItemsOwnedBy(user.id());

    Set<UUID> itemIds = itemsById.keySet();

    Page<Offer> offers = status != null
        ? offerRepository.findByItemIdInAndStatusAndBuyerVisibility(itemIds, status, UserVisibility.VISIBLE, pageable)
        : offerRepository.findByItemIdInAndBuyerVisibility(itemIds, UserVisibility.VISIBLE, pageable);

    Set<UUID> buyerIds = offers.getContent().stream().map(offer -> offer.getBuyerId()).collect(Collectors.toSet());
    Map<UUID, UserSummaryWithEmailDto> buyerById = userOfferApi.getUserSummaryWithEmailByIds(buyerIds);

    return offers.map(offer -> offerMapper.toWithEmailDto(offer, itemsById.get(offer.getItemId()),
        buyerById.get(offer.getBuyerId())));

  }

  @ApplicationModuleListener
  void on(BanEvent event) {
    offerRepository.updateBuyerVisibility(event.ids(), UserVisibility.BANNED, OfferStatus.PENDING);
  }

  @ApplicationModuleListener
  void on(UnbanEvent event) {
    offerRepository.updateBuyerVisibility(event.ids(), UserVisibility.VISIBLE, OfferStatus.PENDING);
  }
}
