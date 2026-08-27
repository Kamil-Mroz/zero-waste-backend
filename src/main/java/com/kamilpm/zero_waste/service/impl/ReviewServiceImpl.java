package com.kamilpm.zero_waste.service.impl;

import com.kamilpm.zero_waste.repository.ReviewRepository;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kamilpm.zero_waste.domain.dto.ReviewDto;
import com.kamilpm.zero_waste.domain.entity.ItemState;
import com.kamilpm.zero_waste.domain.entity.ModerationStatus;
import com.kamilpm.zero_waste.domain.entity.Offer;
import com.kamilpm.zero_waste.domain.entity.OfferStatus;
import com.kamilpm.zero_waste.domain.entity.Review;
import com.kamilpm.zero_waste.domain.entity.User;
import com.kamilpm.zero_waste.domain.entity.UserRole;
import com.kamilpm.zero_waste.domain.mapper.ReviewMapper;
import com.kamilpm.zero_waste.domain.request.ReviewRequest;
import com.kamilpm.zero_waste.domain.response.ReviewResponse;
import com.kamilpm.zero_waste.exception.EntityNotFoundException;
import com.kamilpm.zero_waste.exception.ForbiddenException;
import com.kamilpm.zero_waste.service.AuthService;
import com.kamilpm.zero_waste.service.OfferService;
import com.kamilpm.zero_waste.service.ReportService;
import com.kamilpm.zero_waste.service.ReviewService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {
  private final ReviewRepository reviewRepository;
  private final AuthService authService;
  private final OfferService offerService;
  private final ReviewMapper reviewMapper;
  private final ReportService reportService;

  @Override
  @Transactional
  public ReviewDto createReview(ReviewRequest reviewRequest) {
    User user = authService.getRequiredAuthenticatedUser();

    Offer offer = offerService.getOfferById(reviewRequest.getOfferId());

    if (!Objects.equals(offer.getStatus(), OfferStatus.ACCEPTED)
        || !Objects.equals(offer.getItem().getState(), ItemState.GIVEN)) {
      throw new ForbiddenException("Can not review an unaccepted offer");
    }

    if (!Objects.equals(user.getId(), offer.getBuyer().getId())
        || Objects.equals(user.getId(), offer.getItem().getOwner().getId())) {
      throw new ForbiddenException("Can not leave a review for this offer");
    }
    if (reviewRepository.existsByOffer_Id(offer.getId()))
      throw new ForbiddenException("You have already review this offer");

    Review newReview = Review.builder()
        .comment(reviewRequest.getComment())
        .offer(offer)
        .rating(reviewRequest.getRating())
        .reviewee(offer.getItem().getOwner())
        .reviewer(user)
        .build();

    Review savedReview = reviewRepository.save(newReview);
    return reviewMapper.toDto(savedReview);

  }

  @Override
  @Transactional(readOnly = true)
  public Page<ReviewResponse> getReceivedReviews(Pageable pageable) {
    User user = authService.getRequiredAuthenticatedUser();

    return reviewRepository
        .findByReviewee_IdAndModerationStatusOrderByCreatedAtDesc(user.getId(), ModerationStatus.VISIBLE, pageable)
        .map(reviewMapper::toResponse);
  }

  @Override
  @Transactional(readOnly = true)
  public Page<ReviewResponse> getGivenReviews(Pageable pageable) {
    User user = authService.getRequiredAuthenticatedUser();
    return reviewRepository.findByReviewer_Id(user.getId(), pageable).map(reviewMapper::toResponse);
  }

  @Override
  @Transactional(readOnly = true)
  public Page<ReviewResponse> getUserReviews(UUID userId, Pageable pageable) {

    return reviewRepository
        .findByReviewee_IdAndModerationStatusOrderByCreatedAtDesc(userId, ModerationStatus.VISIBLE, pageable)
        .map(reviewMapper::toResponse);
  }

  @Override
  public void deleteAllByUserIds(List<UUID> ids) {
    reviewRepository.deleteByReviewer_IdIn(ids);
    reviewRepository.deleteByReviewee_IdIn(ids);

  }

  @Override
  public ReviewResponse getReview(UUID id) {
    Review review = reviewRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Review not found"));
    if (Objects.equals(review.getModerationStatus(), ModerationStatus.VISIBLE)) {
      return reviewMapper.toResponse(review);
    }

    User user = authService.getRequiredAuthenticatedUser();

    if (Objects.equals(review.getReviewer().getId(), user.getId())) {
      return reviewMapper.toResponse(review);
    }

    if (Objects.equals(user.getRole(), UserRole.ADMIN)) {
      return reviewMapper.toResponse(review);
    }

    throw new EntityNotFoundException("Review not available");
  }

  @Override
  public void deleteReview(UUID id) {
    User user = authService.getRequiredAuthenticatedUser();

    Review review = reviewRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Review not found"));

    if (!Objects.equals(user.getId(), review.getReviewer().getId()) && user.getRole() != UserRole.ADMIN) {
      throw new ForbiddenException("Only the owner of the review can delete");
    }

    reviewRepository.deleteById(id);
    reportService.rejectAllBySubjectId(id, user.getRole() == UserRole.ADMIN);

  }
}
