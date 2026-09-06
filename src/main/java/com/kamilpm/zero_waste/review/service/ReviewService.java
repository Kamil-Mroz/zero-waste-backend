package com.kamilpm.zero_waste.review.service;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kamilpm.zero_waste.auth.api.AuthApi;
import com.kamilpm.zero_waste.auth.api.AuthenticatedUser;
import com.kamilpm.zero_waste.common.dto.UserSummaryDto;
import com.kamilpm.zero_waste.common.entity.ModerationStatus;
import com.kamilpm.zero_waste.common.exception.EntityNotFoundException;
import com.kamilpm.zero_waste.common.exception.ForbiddenException;
import com.kamilpm.zero_waste.item.api.ItemDto;
import com.kamilpm.zero_waste.item.api.ItemReviewApi;
import com.kamilpm.zero_waste.item.api.SimpleItemDto;
import com.kamilpm.zero_waste.moderation.api.RejectReportEvent;
import com.kamilpm.zero_waste.offer.api.DeleteOffersEvent;
import com.kamilpm.zero_waste.offer.api.OfferDto;
import com.kamilpm.zero_waste.offer.api.OfferReviewApi;
import com.kamilpm.zero_waste.offer.api.SimpleOfferDto;
import com.kamilpm.zero_waste.review.api.ReviewResponse;
import com.kamilpm.zero_waste.review.dto.ReviewDto;
import com.kamilpm.zero_waste.review.dto.ReviewRequest;
import com.kamilpm.zero_waste.review.entity.Review;
import com.kamilpm.zero_waste.review.mapper.ReviewMapper;
import com.kamilpm.zero_waste.review.repository.ReviewRepository;
import com.kamilpm.zero_waste.user.api.UserReviewApi;
import com.kamilpm.zero_waste.user.api.UserRole;
import com.kamilpm.zero_waste.user.api.UsersDeletedEvent;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReviewService {
  private final ReviewRepository reviewRepository;
  private final AuthApi authApi;
  // private final OfferService offerService;
  private final ReviewMapper reviewMapper;
  private final ApplicationEventPublisher events;
  // private final ReportService reportService;
  private final OfferReviewApi offerReviewApi;
  private final ItemReviewApi itemReviewApi;
  private final UserReviewApi userReviewApi;

  @Transactional
  public ReviewDto createReview(ReviewRequest reviewRequest) {
    AuthenticatedUser user = authApi.getRequiredAuthenticatedUser();

    SimpleOfferDto offer = offerReviewApi.getOfferById(reviewRequest.getOfferId());
    SimpleItemDto item = itemReviewApi.getItemById(offer.itemId());

    if (reviewRepository.existsByOfferId(offer.id()))
      throw new ForbiddenException("You have already review this offer");

    UserSummaryDto itemOwner = userReviewApi.getUserById(item.ownerId());

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
    AuthenticatedUser user = authApi.getRequiredAuthenticatedUser();

    Page<Review> reviews = reviewRepository
        .findByRevieweeIdAndModerationStatusOrderByCreatedAtDesc(user.id(), ModerationStatus.VISIBLE, pageable);

    Map<UUID, UserSummaryDto> usersById = userReviewApi
        .getUsersById(reviews.getContent().stream().map(review -> review.getReviewerId()).collect(Collectors.toSet()));

    return reviews.map(review -> reviewMapper.toResponse(review, usersById.get(review.getReviewerId()).getNickname()));
  }

  @Transactional(readOnly = true)
  public Page<ReviewResponse> getGivenReviews(Pageable pageable) {
    AuthenticatedUser user = authApi.getRequiredAuthenticatedUser();
    return reviewRepository.findByReviewerId(user.id(), pageable)
        .map(review -> reviewMapper.toResponse(review, user.nickname()));
  }

  @Transactional(readOnly = true)
  public Page<ReviewResponse> getUserReviews(UUID userId, Pageable pageable) {

    Page<Review> reviews = reviewRepository
        .findByRevieweeIdAndModerationStatusOrderByCreatedAtDesc(userId, ModerationStatus.VISIBLE, pageable);

    Map<UUID, UserSummaryDto> usersById = userReviewApi
        .getUsersById(reviews.getContent().stream().map(review -> review.getReviewerId()).collect(Collectors.toSet()));

    return reviews.map(review -> reviewMapper.toResponse(review, usersById.get(review.getReviewerId()).getNickname()));
  }

  public ReviewResponse getReview(UUID id) {
    Review review = reviewRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Review not found"));
    UserSummaryDto reviewer = userReviewApi.getUserById(review.getReviewerId());

    if (Objects.equals(review.getModerationStatus(), ModerationStatus.VISIBLE)) {
      return reviewMapper.toResponse(review, reviewer.getNickname());
    }

    AuthenticatedUser user = authApi.getRequiredAuthenticatedUser();

    if (Objects.equals(review.getReviewerId(), user.id())) {
      return reviewMapper.toResponse(review, reviewer.getNickname());
    }

    if (Objects.equals(user.role(), UserRole.ADMIN)) {
      return reviewMapper.toResponse(review, reviewer.getNickname());
    }

    throw new EntityNotFoundException("Review not available");
  }

  public void deleteReview(UUID id) {
    AuthenticatedUser user = authApi.getRequiredAuthenticatedUser();

    Review review = reviewRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Review not found"));

    boolean isAdmin = user.role() == UserRole.ADMIN;

    if (!Objects.equals(user.id(), review.getReviewerId()) && !isAdmin) {
      throw new ForbiddenException("Only the owner of the review can delete");
    }

    reviewRepository.deleteById(id);

    events.publishEvent(new RejectReportEvent(id, isAdmin));

  }

  @ApplicationModuleListener
  void on(UsersDeletedEvent event) {
    reviewRepository.deleteByReviewerIdIn(event.ids());
    reviewRepository.deleteByRevieweeIdIn(event.ids());
  }

  @ApplicationModuleListener
  void on(DeleteOffersEvent event) {
    reviewRepository.deleteByOfferIdIn(event.offerIds());
  }

}
