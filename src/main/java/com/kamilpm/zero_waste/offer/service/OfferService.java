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

import com.kamilpm.zero_waste.auth.api.AuthApi;
import com.kamilpm.zero_waste.auth.api.AuthenticatedUser;
import com.kamilpm.zero_waste.common.dto.UserSummaryWithEmailDto;
import com.kamilpm.zero_waste.common.entity.ModerationStatus;
import com.kamilpm.zero_waste.common.exception.ConflictException;
import com.kamilpm.zero_waste.common.exception.EntityNotFoundException;
import com.kamilpm.zero_waste.common.exception.ForbiddenException;
import com.kamilpm.zero_waste.item.api.DeleteItemEvent;
import com.kamilpm.zero_waste.item.api.DeleteItemsEvent;
import com.kamilpm.zero_waste.item.api.ItemDto;
import com.kamilpm.zero_waste.item.api.ItemOfferApi;
import com.kamilpm.zero_waste.item.api.ItemState;
import com.kamilpm.zero_waste.item.api.SimpleItemDto;
import com.kamilpm.zero_waste.notification.api.NotificationRecipient;
import com.kamilpm.zero_waste.notification.api.NotificationReferenceType;
import com.kamilpm.zero_waste.notification.api.NotificationType;
import com.kamilpm.zero_waste.notification.api.SendNotificationEvent;
import com.kamilpm.zero_waste.notification.api.SendNotificationsEvent;
import com.kamilpm.zero_waste.offer.api.DeleteOffersEvent;
import com.kamilpm.zero_waste.offer.api.OfferAcceptEvent;
import com.kamilpm.zero_waste.offer.api.OfferDto;
import com.kamilpm.zero_waste.offer.dto.OfferWithEmailDto;
import com.kamilpm.zero_waste.offer.entity.Offer;
import com.kamilpm.zero_waste.offer.entity.OfferStatus;
import com.kamilpm.zero_waste.offer.mapper.OfferMapper;
import com.kamilpm.zero_waste.offer.repository.OfferRepository;
import com.kamilpm.zero_waste.user.api.UserOfferApi;
import com.kamilpm.zero_waste.user.api.UsersDeletedEvent;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OfferService {
  private final OfferRepository offerRepository;
  private final ItemOfferApi itemOfferApi;
  private final AuthApi authApi;
  // private final NotificationService notificationService;
  private final OfferMapper offerMapper;
  private final ApplicationEventPublisher events;
  private final UserOfferApi userOfferApi;

  public Offer getOfferById(UUID id) {
    Offer offer = offerRepository.findDetailsById(id).orElseThrow(() -> new EntityNotFoundException("Offer not found"));
    return offer;
  }

  private void ensurePending(Offer offer) {
    if (offer.getStatus() != OfferStatus.PENDING)
      throw new ConflictException("Offer is no longer pending");
  }

  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void acceptOffer(UUID id) {
    AuthenticatedUser user = authApi.getRequiredAuthenticatedUser();
    Offer offer = getOfferById(id);
    ensurePending(offer);

    SimpleItemDto item = itemOfferApi.findByIdForUpdate(offer.getItemId());

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
            new SendNotificationEvent(offer.getBuyerId(), buyerEmail, NotificationType.OFFER_ACCEPTED, "Offer accepted",
                "Your request was accepted.", offer.getId(), NotificationReferenceType.OFFER));

    List<NotificationRecipient> rejectedBuyers = userOfferApi
        .getUsersEmail(rejectedOffers.stream().map(o -> o.getBuyerId()).toList());

    events.publishEvent(
        new SendNotificationsEvent(rejectedBuyers, NotificationType.OFFER_REJECTED, "Offer rejected",
            "Your request was declined.", offer.getId(), NotificationReferenceType.OFFER));
  }

  @Transactional
  public void rejectOffer(UUID id) {
    AuthenticatedUser user = authApi.getRequiredAuthenticatedUser();

    Offer offer = getOfferById(id);
    UUID buyerId = offer.getBuyerId();
    SimpleItemDto item = itemOfferApi.findById(id);
    if (!Objects.equals(item.ownerId(), user.id()))
      throw new ForbiddenException("Unable to reject an offer that you are not the owner of item");
    ensurePending(offer);

    String buyerEmail = userOfferApi.getUserEmail(buyerId);

    offer.setStatus(OfferStatus.REJECTED);
    offerRepository.save(offer);

    events.publishEvent(
        new SendNotificationEvent(buyerId, buyerEmail, NotificationType.OFFER_REJECTED, "Offer rejected",
            "Your request was declined.", offer.getId(), NotificationReferenceType.OFFER));

  }

  @Transactional
  public void makeOffer(UUID id) {
    AuthenticatedUser user = authApi.getRequiredAuthenticatedUser();

    SimpleItemDto item = itemOfferApi.findByIdForUpdate(id);

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
    AuthenticatedUser user = authApi.getRequiredAuthenticatedUser();

    Offer offer = getOfferById(id);

    SimpleItemDto item = itemOfferApi.findByIdForUpdate(offer.getItemId());
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

    AuthenticatedUser user = authApi.getRequiredAuthenticatedUser();
    Page<Offer> offers = status != null ? offerRepository.findByBuyerIdAndStatus(user.id(), status, pageable)
        : offerRepository.findByBuyerId(user.id(), pageable);

    Map<UUID, ItemDto> itemsById = itemOfferApi.getItemsByIds(
        offers.getContent().stream().map(offer -> offer.getItemId()).collect(Collectors.toSet()));

    return offers.map(offer -> offerMapper.toDto(offer, itemsById.get(offer.getItemId()),
        null));

  }

  @Transactional
  public Page<OfferWithEmailDto> getReceivedOffers(Pageable pageable, OfferStatus status) {
    AuthenticatedUser user = authApi.getRequiredAuthenticatedUser();
    Map<UUID, ItemDto> itemsById = itemOfferApi.getItemsOwnedBy(user.id());

    Set<UUID> itemIds = itemsById.keySet();

    Page<Offer> offers = status != null ? offerRepository.findByItemIdInAndStatus(itemIds, status, pageable)
        : offerRepository.findByItemIdIn(itemIds, pageable);

    Map<UUID, UserSummaryWithEmailDto> buyerById = userOfferApi
        .getUsersByIds(offers.getContent().stream().map(offer -> offer.getBuyerId()).collect(Collectors.toSet()));

    return offers.map(offer -> offerMapper.toWithEmailDto(offer, itemsById.get(offer.getItemId()),
        buyerById.get(offer.getBuyerId())));

  }

  @ApplicationModuleListener
  void on(DeleteItemEvent event) {
    Set<UUID> offerIds = offerRepository.findByItemId(event.itemId()).stream().map(offer -> offer.getId())
        .collect(Collectors.toSet());
    offerRepository.deleteAllById(offerIds);
    events.publishEvent(new DeleteOffersEvent(offerIds));
  }

  @ApplicationModuleListener
  void on(DeleteItemsEvent event) {
    offerRepository.deleteByItemIdIn(event.itemIds());
  }

  @ApplicationModuleListener
  void on(UsersDeletedEvent event) {
    offerRepository.deleteByBuyerIdIn(event.ids());
  }

}
