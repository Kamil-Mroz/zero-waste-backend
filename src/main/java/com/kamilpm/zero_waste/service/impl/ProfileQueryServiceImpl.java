package com.kamilpm.zero_waste.service.impl;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kamilpm.zero_waste.domain.dto.ItemCountBreakDown;
import com.kamilpm.zero_waste.domain.dto.ProfileItemSummary;
import com.kamilpm.zero_waste.domain.dto.ProfileQueryData;
import com.kamilpm.zero_waste.domain.dto.ProfileReviewSummary;
import com.kamilpm.zero_waste.domain.dto.RatingBreakdown;
import com.kamilpm.zero_waste.domain.entity.Item;
import com.kamilpm.zero_waste.domain.entity.ItemState;
import com.kamilpm.zero_waste.domain.entity.Review;
import com.kamilpm.zero_waste.domain.interfaces.IRatingBreakdownWithStats;
import com.kamilpm.zero_waste.domain.mapper.ItemMapper;
import com.kamilpm.zero_waste.domain.mapper.ReviewMapper;
import com.kamilpm.zero_waste.repository.ItemRepository;
import com.kamilpm.zero_waste.repository.ReviewRepository;
import com.kamilpm.zero_waste.service.ProfileQueryService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProfileQueryServiceImpl implements ProfileQueryService {

  private final ItemRepository itemRepository;
  private final ReviewRepository reviewRepository;
  private final ItemMapper itemMapper;
  private final ReviewMapper reviewMapper;

  @Override
  public ProfileQueryData getPublicProfileData(UUID userId) {
    ProfileItemSummary items = buildItemSummary(userId);
    ProfileReviewSummary reviews = buildReviewSummary(userId);
    return new ProfileQueryData(items, reviews);

  }

  private ProfileItemSummary buildItemSummary(UUID userId) {
    List<Item> latestItems = itemRepository.findTop3ByOwner_IdAndStateOrderByCreatedAtDesc(userId, ItemState.AVAILABLE);

    ItemCountBreakDown itemCountBreakDown = buildItemCountBreakDown(userId);
    return ProfileItemSummary.builder()
        .latestItems(latestItems.stream().map(itemMapper::toDto).toList())
        .itemCountBreakDown(itemCountBreakDown).build();
  }

  private ItemCountBreakDown buildItemCountBreakDown(UUID userId) {
    long given = 0, available = 0, pending = 0;
    for (var row : itemRepository.countTotalItemsByOwnerIdAndState(userId)) {
      switch (row.getItemState()) {
        case GIVEN -> given = row.getTotalItem();
        case AVAILABLE -> available = row.getTotalItem();
        case PENDING -> pending = row.getTotalItem();
      }
    }
    return ItemCountBreakDown.builder()
        .available(available)
        .pending(pending)
        .given(given)
        .totalItems(available + pending + given)
        .build();

  }

  private ProfileReviewSummary buildReviewSummary(UUID userId) {
    long one = 0, two = 0, three = 0, four = 0, five = 0, count = 0;
    Double avg = 0.0;

    for (IRatingBreakdownWithStats row : reviewRepository.getRatingBreakdownWithStats(userId)) {
      count = row.getTotalCount();
      avg = row.getAvgRating();
      switch (row.getRating()) {
        case 1 -> one = row.getCount();
        case 2 -> two = row.getCount();
        case 3 -> three = row.getCount();
        case 4 -> four = row.getCount();
        case 5 -> five = row.getCount();
      }
    }

    List<Review> latestReviews = reviewRepository.findTop3ByReviewee_IdOrderByCreatedAtDesc(userId);

    return ProfileReviewSummary.builder()
        .averageRating(avg == null ? 0.0 : avg)
        .reviewCount(count)
        .latestReviews(latestReviews.stream().map(reviewMapper::toResponse).toList())
        .ratingBreakdown(RatingBreakdown.builder()
            .oneStar(one)
            .twoStar(two)
            .threeStar(three)
            .fourStar(four)
            .fiveStar(five)
            .build())
        .build();

  }

}
