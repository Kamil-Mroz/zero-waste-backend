package com.kamilpm.zero_waste.review.service;

import java.util.Collection;
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
import org.springframework.transaction.annotation.Transactional;

import com.kamilpm.zero_waste.auth.api.CurrentUserApi;
import com.kamilpm.zero_waste.common.entity.ModerationStatus;
import com.kamilpm.zero_waste.common.exception.EntityNotFoundException;
import com.kamilpm.zero_waste.common.exception.ForbiddenException;
import com.kamilpm.zero_waste.common.utils.OwnMapper;
import com.kamilpm.zero_waste.item.api.ItemReviewApi;
import com.kamilpm.zero_waste.moderation.api.RejectReportEvent;
import com.kamilpm.zero_waste.offer.api.DeleteOffersEvent;
import com.kamilpm.zero_waste.offer.api.OfferReviewApi;
import com.kamilpm.zero_waste.review.dto.OfferStatus;
import com.kamilpm.zero_waste.review.dto.AuthenticatedUser;
import com.kamilpm.zero_waste.review.dto.ItemCondition;
import com.kamilpm.zero_waste.review.dto.ItemDto;
import com.kamilpm.zero_waste.review.dto.ItemState;
import com.kamilpm.zero_waste.review.dto.OfferDto;
import com.kamilpm.zero_waste.review.dto.ReviewDto;
import com.kamilpm.zero_waste.review.dto.ReviewRequest;
import com.kamilpm.zero_waste.review.dto.ReviewResponse;
import com.kamilpm.zero_waste.review.dto.SimpleItemDto;
import com.kamilpm.zero_waste.review.dto.SimpleOfferDto;
import com.kamilpm.zero_waste.review.dto.UserRole;
import com.kamilpm.zero_waste.review.dto.UserSummaryDto;
import com.kamilpm.zero_waste.review.entity.Review;
import com.kamilpm.zero_waste.review.mapper.ReviewMapper;
import com.kamilpm.zero_waste.review.repository.ReviewRepository;
import com.kamilpm.zero_waste.user.api.UserReviewApi;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReviewService {
  private final ReviewRepository reviewRepository;
  private final CurrentUserApi currentUser;
  private final ReviewMapper reviewMapper;
  private final ApplicationEventPublisher events;
  private final OfferReviewApi offerReviewApi;
  private final ItemReviewApi itemReviewApi;
  private final UserReviewApi userReviewApi;

  @Transactional
  public ReviewDto createReview(ReviewRequest reviewRequest) {
    AuthenticatedUser user = getRequiredAuthenticatedUser();

    SimpleOfferDto offer = getOfferById(reviewRequest.getOfferId());
    SimpleItemDto item = getItemById(offer.itemId());

    if (reviewRepository.existsByOfferId(offer.id()))
      throw new ForbiddenException("You have already review this offer");

    UserSummaryDto itemOwner = getUserById(item.ownerId());

    Review newReview = Review.builder()
        .comment(reviewRequest.getComment())
        .offerId(offer.id())
        .rating(reviewRequest.getRating())
        .revieweeId(item.ownerId())
        .reviewerId(user.id())
        .build();

    Review savedReview = reviewRepository.save(newReview);

    return reviewMapper.toDto(savedReview,
        new OfferDto(offer.id(),
            new ItemDto(item.id(), item.title(), item.description(), item.city(), item.condition(), item.state(),
                item.moderationStatus(), null, itemOwner, null, null),
            new UserSummaryDto(user.id(), user.nickname()), offer.status()));

  }

  @Transactional(readOnly = true)
  public Page<ReviewResponse> getReceivedReviews(Pageable pageable) {
    AuthenticatedUser user = getRequiredAuthenticatedUser();

    Page<Review> reviews = reviewRepository
        .findByRevieweeIdAndModerationStatusOrderByCreatedAtDesc(user.id(), ModerationStatus.VISIBLE, pageable);
    Set<UUID> reviewerIds = reviews.getContent().stream().map(review -> review.getReviewerId())
        .collect(Collectors.toSet());
    Map<UUID, UserSummaryDto> usersById = getUsersById(reviewerIds);

    return reviews.map(review -> reviewMapper.toResponse(review, usersById.get(review.getReviewerId()).nickname()));
  }

  @Transactional(readOnly = true)
  public Page<ReviewResponse> getGivenReviews(Pageable pageable) {
    AuthenticatedUser user = getRequiredAuthenticatedUser();
    return reviewRepository.findByReviewerId(user.id(), pageable)
        .map(review -> reviewMapper.toResponse(review, user.nickname()));
  }

  @Transactional(readOnly = true)
  public Page<ReviewResponse> getUserReviews(UUID userId, Pageable pageable) {

    Page<Review> reviews = reviewRepository
        .findByRevieweeIdAndModerationStatusOrderByCreatedAtDesc(userId, ModerationStatus.VISIBLE, pageable);
    Set<UUID> reviewerIds = reviews.getContent().stream().map(review -> review.getReviewerId())
        .collect(Collectors.toSet());
    Map<UUID, UserSummaryDto> usersById = getUsersById(reviewerIds);

    return reviews.map(review -> reviewMapper.toResponse(review, usersById.get(review.getReviewerId()).nickname()));
  }

  public ReviewResponse getReview(UUID id) {
    Review review = reviewRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Review not found"));
    UserSummaryDto reviewer = getUserById(review.getReviewerId());

    if (Objects.equals(review.getModerationStatus(), ModerationStatus.VISIBLE)) {
      return reviewMapper.toResponse(review, reviewer.nickname());
    }

    AuthenticatedUser user = getRequiredAuthenticatedUser();

    if (Objects.equals(review.getReviewerId(), user.id())) {
      return reviewMapper.toResponse(review, reviewer.nickname());
    }

    if (Objects.equals(user.role(), UserRole.ADMIN)) {
      return reviewMapper.toResponse(review, reviewer.nickname());
    }

    throw new EntityNotFoundException("Review not available");
  }

  public void deleteReview(UUID id) {
    AuthenticatedUser user = getRequiredAuthenticatedUser();

    Review review = reviewRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Review not found"));

    boolean isAdmin = user.role() == UserRole.ADMIN;

    if (!Objects.equals(user.id(), review.getReviewerId()) && !isAdmin) {
      throw new ForbiddenException("Only the owner of the review can delete");
    }

    reviewRepository.deleteById(id);

    events.publishEvent(new RejectReportEvent(id, isAdmin));

  }

  @ApplicationModuleListener
  void on(DeleteOffersEvent event) {
    reviewRepository.deleteByOfferIdIn(event.offerIds());
  }

  private AuthenticatedUser getRequiredAuthenticatedUser() {
    return OwnMapper.map(currentUser.getRequiredAuthenticatedUser(), (user) -> new AuthenticatedUser(
        user.id(),
        user.email(),
        user.nickname(),
        user.password(),
        UserRole.valueOf(user.role().name()),
        user.banActive(),
        user.bannedUntil(),
        user.joinedAt()));
  }

  private SimpleOfferDto getOfferById(UUID id) {
    return OwnMapper.map(offerReviewApi.getOfferById(id), (offer) -> new SimpleOfferDto(
        offer.id(), offer.itemId(), offer.buyerId(), OfferStatus.valueOf(offer.status().name())));
  }

  private SimpleItemDto getItemById(UUID id) {
    return OwnMapper.map(
        itemReviewApi.getItemById(id), (item) -> new SimpleItemDto(item.id(), item.title(), item.description(),
            item.city(), ItemCondition.valueOf(item.condition().name()), ItemState.valueOf(item.state().name()),
            item.moderationStatus(),
            item.ownerId()));
  }

  private UserSummaryDto getUserById(UUID id) {
    return OwnMapper.map(
        userReviewApi.getUserById(id), (user) -> new UserSummaryDto(user.id(), user.nickname()));
  }

  private Map<UUID, UserSummaryDto> getUsersById(Collection<UUID> ids) {
    return OwnMapper.mapValues(
        userReviewApi
            .getUsersById(ids),

        (user) -> new UserSummaryDto(user.id(), user.nickname()));
  }
}
