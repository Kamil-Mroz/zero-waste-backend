package com.kamilpm.zero_waste.service.impl;

import com.kamilpm.zero_waste.repository.ReviewRepository;
import com.kamilpm.zero_waste.security.MyUserDetails;

import java.util.Objects;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.kamilpm.zero_waste.domain.entity.ItemState;
import com.kamilpm.zero_waste.domain.entity.Offer;
import com.kamilpm.zero_waste.domain.entity.OfferStatus;
import com.kamilpm.zero_waste.domain.entity.Review;
import com.kamilpm.zero_waste.domain.entity.User;
import com.kamilpm.zero_waste.domain.mapper.ReviewMapper;
import com.kamilpm.zero_waste.domain.request.ReviewRequest;
import com.kamilpm.zero_waste.domain.response.ReviewResponse;
import com.kamilpm.zero_waste.exception.ForbiddenException;
import com.kamilpm.zero_waste.service.AuthService;
import com.kamilpm.zero_waste.service.OfferService;
import com.kamilpm.zero_waste.service.ReviewService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {
  private final ReviewRepository reviewRepository;
  private final AuthService authService;
  private final OfferService offerService;
  private final ReviewMapper reviewMapper;

  @Override
  public Review createReview(ReviewRequest reviewRequest) {
    User user = authService.getRequiredAuthenticatedUserEntity();

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

    return reviewRepository.save(newReview);

  }

  @Override
  public Page<ReviewResponse> getReceivedReviews(Pageable pageable) {
    MyUserDetails user = authService.getRequiredAuthenticatedUserDetails();

    return reviewRepository.findByReviewee_IdOrderByCreatedAtDesc(user.getId(), pageable).map(reviewMapper::toResponse);
  }

  @Override
  public Page<ReviewResponse> getGivenReviews(Pageable pageable) {

    MyUserDetails user = authService.getRequiredAuthenticatedUserDetails();

    return reviewRepository.findByReviewer_Id(user.getId(), pageable).map(reviewMapper::toResponse);
  }

  @Override
  public Page<ReviewResponse> getUserReviews(UUID userId, Pageable pageable) {

    return reviewRepository.findByReviewee_IdOrderByCreatedAtDesc(userId, pageable).map(reviewMapper::toResponse);
  }
}
