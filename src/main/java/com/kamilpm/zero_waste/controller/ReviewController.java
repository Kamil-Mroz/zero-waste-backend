package com.kamilpm.zero_waste.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kamilpm.zero_waste.domain.entity.Review;
import com.kamilpm.zero_waste.domain.request.ReviewRequest;
import com.kamilpm.zero_waste.domain.response.PageResponse;
import com.kamilpm.zero_waste.domain.response.ReviewResponse;
import com.kamilpm.zero_waste.service.ReviewService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@RestController
@RequestMapping(path = "/api/v{version}/reviews", version = "1")
@RequiredArgsConstructor
public class ReviewController {

  private final ReviewService reviewService;

  @PostMapping
  public ResponseEntity<Review> createReview(@Valid @RequestBody ReviewRequest reviewRequest) {

    Review review = reviewService.createReview(reviewRequest);

    return ResponseEntity.ok(review);
  }

  @GetMapping("/received")
  public ResponseEntity<PageResponse<ReviewResponse>> getReceivedReviews(
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size) {
    Page<ReviewResponse> reviews = reviewService.getReceivedReviews(PageRequest.of(page, size));
    return ResponseEntity.ok(PageResponse.<ReviewResponse>builder()
        .content(reviews.getContent())
        .page(reviews.getNumber())
        .size(reviews.getSize())
        .totalElements(reviews.getTotalElements())
        .totalPages(reviews.getTotalPages())
        .build());
  }

  @GetMapping("/given")
  public ResponseEntity<PageResponse<ReviewResponse>> getGivenReview(
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size) {
    Page<ReviewResponse> reviews = reviewService.getGivenReviews(PageRequest.of(page, size));
    return ResponseEntity.ok(PageResponse.<ReviewResponse>builder()
        .content(reviews.getContent())
        .page(reviews.getNumber())
        .size(reviews.getSize())
        .totalElements(reviews.getTotalElements())
        .totalPages(reviews.getTotalPages())
        .build());
  }

  @GetMapping("/user/{id}")
  public ResponseEntity<PageResponse<ReviewResponse>> getUserReviews(
      @PathVariable(value = "id") UUID userId,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size) {
    Page<ReviewResponse> reviews = reviewService.getUserReviews(userId, PageRequest.of(page, size));
    return ResponseEntity.ok(PageResponse.<ReviewResponse>builder()
        .content(reviews.getContent())
        .page(reviews.getNumber())
        .size(reviews.getSize())
        .totalElements(reviews.getTotalElements())
        .totalPages(reviews.getTotalPages())
        .build());
  }

}
