package com.kamilpm.zero_waste.service.impl;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.kamilpm.zero_waste.domain.dto.ItemCountBreakDown;
import com.kamilpm.zero_waste.domain.dto.ItemDto;
import com.kamilpm.zero_waste.domain.dto.ProfileItemSummary;
import com.kamilpm.zero_waste.domain.dto.ProfileReviewSummary;
import com.kamilpm.zero_waste.domain.dto.RatingBreakdown;
import com.kamilpm.zero_waste.domain.entity.ItemState;
import com.kamilpm.zero_waste.domain.entity.User;
import com.kamilpm.zero_waste.domain.interfaces.IItemCount;
import com.kamilpm.zero_waste.domain.interfaces.IRatingCountProjection;
import com.kamilpm.zero_waste.domain.mapper.ItemMapper;
import com.kamilpm.zero_waste.domain.mapper.ReviewMapper;
import com.kamilpm.zero_waste.domain.response.OwnProfileResponse;
import com.kamilpm.zero_waste.domain.response.PublicUserProfileResponse;
import com.kamilpm.zero_waste.domain.response.ReviewResponse;
import com.kamilpm.zero_waste.repository.ItemRepository;
import com.kamilpm.zero_waste.repository.ReviewRepository;
import com.kamilpm.zero_waste.security.MyUserDetails;
import com.kamilpm.zero_waste.service.AuthService;
import com.kamilpm.zero_waste.service.ProfileService;
import com.kamilpm.zero_waste.service.UserService;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Transactional
@Service
@RequiredArgsConstructor
public class ProfileServiceImpl implements ProfileService {

  private final UserService userService;
  private final AuthService authService;
  private final ReviewRepository reviewRepository;
  private final ItemRepository itemRepository;
  private final ReviewMapper reviewMapper;
  private final ItemMapper itemMapper;

  @Override
  public PublicUserProfileResponse getProfile(UUID userId) {

    User user = userService.getUser(userId);

    return PublicUserProfileResponse.builder()
        .id(user.getId())
        .firstName(user.getFirstName())
        .lastName(user.getLastName())
        .joinedAt(user.getJoinedAt())
        .items(buildItemSummary(user.getId()))
        .reviews(buildReviewSummary(userId))
        .build();
  }

  @Override
  public OwnProfileResponse getOwnProfile() {
    MyUserDetails user = authService.getRequiredAuthenticatedUserDetails();

    return OwnProfileResponse.builder()
        .items(buildItemSummary(user.getId()))
        .reviews(buildReviewSummary(user.getId()))
        .build();
  }

  private ProfileItemSummary buildItemSummary(UUID userId) {

    List<ItemDto> latestItems = itemRepository
        .findTop3ByOwner_IdAndStateOrderByCreatedAtDesc(userId, ItemState.AVAILABLE)
        .stream()
        .map(itemMapper::toDto).toList();

    return ProfileItemSummary.builder()
        .latestItems(latestItems)
        .itemCountBreakDown(buildItemCountBreakDown(userId))
        .build();
  }

  private ItemCountBreakDown buildItemCountBreakDown(UUID userId) {
    long given = 0, available = 0, pending = 0;

    for (IItemCount row : itemRepository.countTotalItemsByOwnerIdAndState(userId)) {
      switch (row.getItemState()) {
        case ItemState.GIVEN:
          given = row.getTotalItem();
          break;
        case ItemState.AVAILABLE:
          available = row.getTotalItem();
          break;
        case ItemState.PENDING:
          pending = row.getTotalItem();
          break;

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
    Double avg = reviewRepository.getAverageRating(userId);

    long count = reviewRepository.countByReviewee_Id(userId);
    List<ReviewResponse> latestReviews = reviewRepository.findTop3ByReviewee_IdOrderByCreatedAtDesc(userId).stream()
        .map(reviewMapper::toResponse).toList();

    return ProfileReviewSummary.builder()
        .averageRating(avg == null ? 0.0 : avg)
        .reviewCount(count)
        .latestReviews(latestReviews)
        .ratingBreakdown(buildRatingBreakdown(userId))
        .build();
  }

  private RatingBreakdown buildRatingBreakdown(UUID userId) {
    long one = 0, two = 0, three = 0, four = 0, five = 0;

    for (IRatingCountProjection row : reviewRepository.getRatingBreakdown(userId)) {
      switch (row.getRating()) {
        case 1:
          one = row.getCount();
          break;
        case 2:
          two = row.getCount();
          break;
        case 3:
          three = row.getCount();
          break;
        case 4:
          four = row.getCount();
          break;
        case 5:
          five = row.getCount();
          break;
      }

    }
    return RatingBreakdown.builder()
        .oneStar(one)
        .twoStar(two)
        .threeStar(three)
        .fourStar(four)
        .fiveStar(five)
        .build();
  }
}
