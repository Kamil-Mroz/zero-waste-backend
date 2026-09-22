package com.kamilpm.zero_waste.review.service;

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

import com.kamilpm.zero_waste.common.dto.CurrentUser;
import com.kamilpm.zero_waste.common.dto.OfferData;
import com.kamilpm.zero_waste.common.dto.ReviewData;
import com.kamilpm.zero_waste.common.dto.SimpleItemData;
import com.kamilpm.zero_waste.common.dto.SimpleOfferData;
import com.kamilpm.zero_waste.common.dto.UserRole;
import com.kamilpm.zero_waste.common.dto.UserSummaryDto;
import com.kamilpm.zero_waste.common.dto.UserVisibility;
import com.kamilpm.zero_waste.common.entity.ModerationStatus;
import com.kamilpm.zero_waste.common.events.BanEvent;
import com.kamilpm.zero_waste.common.events.DeleteOffersEvent;
import com.kamilpm.zero_waste.common.events.RejectReportEvent;
import com.kamilpm.zero_waste.common.events.UnbanEvent;
import com.kamilpm.zero_waste.common.events.UserRoleChangeEvent;
import com.kamilpm.zero_waste.common.exception.EntityNotFoundException;
import com.kamilpm.zero_waste.common.exception.ForbiddenException;
import com.kamilpm.zero_waste.common.interfaces.CurrentUserProvider;
import com.kamilpm.zero_waste.common.interfaces.ItemProvider;
import com.kamilpm.zero_waste.common.interfaces.OfferProvider;
import com.kamilpm.zero_waste.common.interfaces.UserProvider;
import com.kamilpm.zero_waste.review.dto.ReviewDto;
import com.kamilpm.zero_waste.review.dto.ReviewRequest;
import com.kamilpm.zero_waste.review.entity.Review;
import com.kamilpm.zero_waste.review.mapper.ReviewMapper;
import com.kamilpm.zero_waste.review.repository.ReviewRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReviewService {
  private final ReviewRepository reviewRepository;
  private final ReviewMapper reviewMapper;
  private final CurrentUserProvider currentUser;
  private final OfferProvider offerReviewApi;
  private final ItemProvider itemReviewApi;
  private final UserProvider userReviewApi;
  private final ApplicationEventPublisher events;

  @Transactional
  public ReviewDto createReview(ReviewRequest reviewRequest) {
    CurrentUser user = currentUser.getRequiredAuthenticatedUser();

    SimpleOfferData offer = offerReviewApi.getOfferById(reviewRequest.getOfferId());
    SimpleItemData item = itemReviewApi.getItemById(offer.itemId());

    if (reviewRepository.existsByOfferId(offer.id()))
      throw new ForbiddenException("You have already review this offer");

    UserSummaryDto itemOwner = userReviewApi.findUserSummaryById(item.ownerId());
    SimpleItemData itemWithOwner = new SimpleItemData(item.id(), item.title(), item.description(), item.city(),
        item.condition(), item.state(), item.moderationStatus(), item.ownerId(), itemOwner);

    Review newReview = Review.builder()
        .comment(reviewRequest.getComment())
        .offerId(offer.id())
        .rating(reviewRequest.getRating())
        .revieweeId(item.ownerId())
        .reviewerId(user.id())
        .reviewerVisibility(UserVisibility.VISIBLE)
        .build();

    Review savedReview = reviewRepository.save(newReview);

    return reviewMapper.toDto(savedReview,
        new OfferData(offer.id(), itemWithOwner,
            new UserSummaryDto(user.id(), user.nickname()), offer.status()));

  }

  @Transactional(readOnly = true)
  public Page<ReviewData> getReceivedReviews(Pageable pageable) {
    CurrentUser user = currentUser.getRequiredAuthenticatedUser();

    Page<Review> reviews = reviewRepository
        .findByRevieweeIdAndModerationStatusAndReviewerVisibilityOrderByCreatedAtDesc(user.id(),
            ModerationStatus.VISIBLE, UserVisibility.VISIBLE, pageable);
    Set<UUID> reviewerIds = reviews.getContent().stream().map(review -> review.getReviewerId())
        .collect(Collectors.toSet());
    Map<UUID, UserSummaryDto> usersById = userReviewApi.getUserSummaryByIds(reviewerIds);

    return reviews.map(review -> reviewMapper.toResponse(review, usersById.get(review.getReviewerId()).nickname()));
  }

  @Transactional(readOnly = true)
  public Page<ReviewData> getGivenReviews(Pageable pageable) {
    CurrentUser user = currentUser.getRequiredAuthenticatedUser();
    return reviewRepository.findByReviewerId(user.id(), pageable)
        .map(review -> reviewMapper.toResponse(review, user.nickname()));
  }

  @Transactional(readOnly = true)
  public Page<ReviewData> getUserReviews(UUID userId, Pageable pageable) {

    Page<Review> reviews = reviewRepository
        .findByRevieweeIdAndModerationStatusAndReviewerVisibilityOrderByCreatedAtDesc(userId, ModerationStatus.VISIBLE,
            UserVisibility.VISIBLE, pageable);
    Set<UUID> reviewerIds = reviews.getContent().stream().map(review -> review.getReviewerId())
        .collect(Collectors.toSet());
    Map<UUID, UserSummaryDto> usersById = userReviewApi.getUserSummaryByIds(reviewerIds);

    return reviews.map(review -> reviewMapper.toResponse(review, usersById.get(review.getReviewerId()).nickname()));
  }

  public ReviewData getReview(UUID id) {
    Review review = reviewRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Review not found"));
    UserSummaryDto reviewer = userReviewApi.findUserSummaryById(review.getReviewerId());

    if (Objects.equals(review.getModerationStatus(), ModerationStatus.VISIBLE)) {
      return reviewMapper.toResponse(review, reviewer.nickname());
    }

    CurrentUser user = currentUser.getRequiredAuthenticatedUser();

    if (Objects.equals(review.getReviewerId(), user.id())) {
      return reviewMapper.toResponse(review, reviewer.nickname());
    }

    if (Objects.equals(user.role(), UserRole.ADMIN)) {
      return reviewMapper.toResponse(review, reviewer.nickname());
    }

    throw new EntityNotFoundException("Review not available");
  }

  public void deleteReview(UUID id) {
    CurrentUser user = currentUser.getRequiredAuthenticatedUser();

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

  @ApplicationModuleListener
  void on(BanEvent event) {
    reviewRepository.updateReviewerVisibility(event.ids(), UserVisibility.BANNED);
  }

  @ApplicationModuleListener
  void on(UnbanEvent event) {
    reviewRepository.updateReviewerVisibility(event.ids(), UserVisibility.VISIBLE);
  }

  @ApplicationModuleListener
  void on(UserRoleChangeEvent event) {

    if (event.newRole() == UserRole.DEMO) {
      reviewRepository.updateReviewerVisibility(event.userId(), UserVisibility.HIDDEN);
    }

    if (event.oldRole() == UserRole.DEMO
        && event.newRole() != UserRole.DEMO) {
      reviewRepository.updateReviewerVisibility(event.userId(), UserVisibility.VISIBLE);
    }
  }

}
