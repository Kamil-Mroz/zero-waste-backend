package com.kamilpm.zero_waste.review.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;

import com.kamilpm.zero_waste.category.entity.Category;
import com.kamilpm.zero_waste.category.repository.CategoryRepository;
import com.kamilpm.zero_waste.common.dto.ItemCondition;
import com.kamilpm.zero_waste.common.dto.ItemState;
import com.kamilpm.zero_waste.common.dto.OfferStatus;
import com.kamilpm.zero_waste.common.dto.UserRole;
import com.kamilpm.zero_waste.common.dto.UserVisibility;
import com.kamilpm.zero_waste.config.PostgresTestConfiguration;
import com.kamilpm.zero_waste.item.entity.Item;
import com.kamilpm.zero_waste.item.repository.ItemRepository;
import com.kamilpm.zero_waste.offer.entity.Offer;
import com.kamilpm.zero_waste.offer.repository.OfferRepository;
import com.kamilpm.zero_waste.review.entity.Review;
import com.kamilpm.zero_waste.review.interfaces.IRatingBreakdownWithStats;
import com.kamilpm.zero_waste.user.entity.User;
import com.kamilpm.zero_waste.user.repository.UserRepository;

@DataJpaTest
@Import(PostgresTestConfiguration.class)
public class ReviewRepositoryTest {

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private CategoryRepository categoryRepository;

  @Autowired
  private ItemRepository itemRepository;

  @Autowired
  private OfferRepository offerRepository;

  @Autowired
  private ReviewRepository reviewRepository;

  @Test
  void testGetRatingBreakdownWithStats() {
    String email = "test@example.com";
    User user1 = User.builder()
        .nickname("test-123")
        .email(email)
        .password(null)
        .role(UserRole.USER)
        .banActive(false)
        .bannedUntil(null)
        .build();
    User savedUser1 = userRepository.save(user1);

    User user2 = User.builder()
        .nickname("test2-123")
        .email("2" + email)
        .password(null)
        .role(UserRole.USER)
        .banActive(false)
        .bannedUntil(null)
        .build();
    User savedUser2 = userRepository.save(user2);

    Category category = Category.builder()
        .name("Sport")
        .parent(null)
        .build();

    Category savedCategory = categoryRepository.save(category);

    Item item1 = Item.builder()
        .title("Title")
        .description("Description")
        .condition(ItemCondition.NEW)
        .state(ItemState.GIVEN)
        .city("City")
        .ownerVisibility(UserVisibility.VISIBLE)
        .categoryId(savedCategory.getId())
        .ownerId(savedUser1.getId())
        .build();

    Item savedItem1 = itemRepository.save(item1);

    Item item2 = Item.builder()
        .title("Title")
        .description("Description")
        .condition(ItemCondition.NEW)
        .state(ItemState.GIVEN)
        .city("City")
        .ownerVisibility(UserVisibility.VISIBLE)
        .categoryId(savedCategory.getId())
        .ownerId(savedUser1.getId())
        .build();

    Item savedItem2 = itemRepository.save(item2);

    Offer offer1 = Offer.builder()
        .buyerId(savedUser2.getId())
        .itemId(savedItem1.getId())
        .buyerVisibility(UserVisibility.VISIBLE)
        .status(OfferStatus.ACCEPTED)
        .build();

    Offer savedOffer1 = offerRepository.save(offer1);

    Offer offer2 = Offer.builder()
        .buyerId(savedUser2.getId())
        .itemId(savedItem2.getId())
        .buyerVisibility(UserVisibility.VISIBLE)
        .status(OfferStatus.ACCEPTED)
        .build();
    Offer savedOffer2 = offerRepository.save(offer2);

    Review review1 = Review.builder()
        .comment("Comment 1")
        .offerId(savedOffer1.getId())
        .rating(5)
        .revieweeId(savedItem1.getOwnerId())
        .reviewerId(savedUser2.getId())
        .reviewerVisibility(UserVisibility.VISIBLE)
        .build();

    reviewRepository.save(review1);

    Review review2 = Review.builder()
        .comment("Comment 1")
        .offerId(savedOffer2.getId())
        .rating(2)
        .revieweeId(savedItem2.getOwnerId())
        .reviewerId(savedUser2.getId())
        .reviewerVisibility(UserVisibility.VISIBLE)
        .build();

    reviewRepository.save(review2);
    long one = 0, two = 0, three = 0, four = 0, five = 0, count = 0, totalRating = 0;

    for (IRatingBreakdownWithStats row : reviewRepository.getRatingBreakdownWithStats(savedUser1.getId(),
        UserVisibility.VISIBLE)) {
      count += row.getCount();
      totalRating += row.getRating() * row.getCount();
      switch (row.getRating()) {
        case 1 -> one = row.getCount();
        case 2 -> two = row.getCount();
        case 3 -> three = row.getCount();
        case 4 -> four = row.getCount();
        case 5 -> five = row.getCount();
      }
    }
    double avg = count == 0 ? 0.0 : (double) totalRating / count;
    assertThat(avg).isEqualTo(3.5);
    assertThat(one).isEqualTo(0);
    assertThat(two).isEqualTo(1);
    assertThat(three).isEqualTo(0);
    assertThat(four).isEqualTo(0);
    assertThat(five).isEqualTo(1);
    assertThat(count).isEqualTo(2);

  }

  @Test
  void testIsReviewerOrReviewee() {

    String email = "test@example.com";
    User user1 = User.builder()
        .nickname("test-123")
        .email(email)
        .password(null)
        .role(UserRole.USER)
        .banActive(false)
        .bannedUntil(null)
        .build();
    User savedUser1 = userRepository.save(user1);

    User user2 = User.builder()
        .nickname("test2-123")
        .email("2" + email)
        .password(null)
        .role(UserRole.USER)
        .banActive(false)
        .bannedUntil(null)
        .build();
    User savedUser2 = userRepository.save(user2);

    Category category = Category.builder()
        .name("Sport")
        .parent(null)
        .build();

    Category savedCategory = categoryRepository.save(category);

    Item item1 = Item.builder()
        .title("Title")
        .description("Description")
        .condition(ItemCondition.NEW)
        .state(ItemState.GIVEN)
        .city("City")
        .ownerVisibility(UserVisibility.VISIBLE)
        .categoryId(savedCategory.getId())
        .ownerId(savedUser1.getId())
        .build();

    Item savedItem1 = itemRepository.save(item1);

    Offer offer1 = Offer.builder()
        .buyerId(savedUser2.getId())
        .itemId(savedItem1.getId())
        .buyerVisibility(UserVisibility.VISIBLE)
        .status(OfferStatus.ACCEPTED)
        .build();

    Offer savedOffer1 = offerRepository.save(offer1);

    Review review1 = Review.builder()
        .comment("Comment 1")
        .offerId(savedOffer1.getId())
        .rating(5)
        .revieweeId(savedItem1.getOwnerId())
        .reviewerId(savedUser2.getId())
        .reviewerVisibility(UserVisibility.VISIBLE)
        .build();

    Review savedReview = reviewRepository.save(review1);

    Boolean isReviewerOrReviewee = reviewRepository.isReviewerOrReviewee(savedReview.getId(), savedUser1.getId());

    assertThat(isReviewerOrReviewee).isTrue();

  }

  @Test
  void testUpdateReviewerVisibility() {
    String email = "test@example.com";
    User user1 = User.builder()
        .nickname("test-123")
        .email(email)
        .password(null)
        .role(UserRole.USER)
        .banActive(false)
        .bannedUntil(null)
        .build();
    User savedUser1 = userRepository.save(user1);

    User user2 = User.builder()
        .nickname("test2-123")
        .email("2" + email)
        .password(null)
        .role(UserRole.USER)
        .banActive(true)
        .bannedUntil(null)
        .build();
    User savedUser2 = userRepository.save(user2);

    Category category = Category.builder()
        .name("Sport")
        .parent(null)
        .build();

    Category savedCategory = categoryRepository.save(category);

    Item item1 = Item.builder()
        .title("Title")
        .description("Description")
        .condition(ItemCondition.NEW)
        .state(ItemState.GIVEN)
        .city("City")
        .ownerVisibility(UserVisibility.BANNED)
        .categoryId(savedCategory.getId())
        .ownerId(savedUser1.getId())
        .build();

    Item savedItem1 = itemRepository.save(item1);

    Offer offer1 = Offer.builder()
        .buyerId(savedUser2.getId())
        .itemId(savedItem1.getId())
        .buyerVisibility(UserVisibility.BANNED)
        .status(OfferStatus.ACCEPTED)
        .build();

    Offer savedOffer1 = offerRepository.save(offer1);

    Review review1 = Review.builder()
        .comment("Comment 1")
        .offerId(savedOffer1.getId())
        .rating(5)
        .revieweeId(savedItem1.getOwnerId())
        .reviewerId(savedUser2.getId())
        .reviewerVisibility(UserVisibility.VISIBLE)
        .build();

    Review savedReview = reviewRepository.save(review1);

    reviewRepository.updateReviewerVisibility(savedUser2.getId(), UserVisibility.BANNED);

    Optional<Review> updatedReview = reviewRepository.findById(savedReview.getId());

    assertThat(updatedReview.isPresent()).isTrue();
    assertThat(updatedReview.get().getReviewerVisibility()).isEqualTo(UserVisibility.BANNED);

  }

  @Test
  void testUpdateReviewerVisibility2() {

    String email = "test@example.com";
    User user1 = User.builder()
        .nickname("test-123")
        .email(email)
        .password(null)
        .role(UserRole.USER)
        .banActive(false)
        .bannedUntil(null)
        .build();
    User savedUser1 = userRepository.save(user1);

    User user2 = User.builder()
        .nickname("test2-123")
        .email("2" + email)
        .password(null)
        .role(UserRole.USER)
        .banActive(true)
        .bannedUntil(null)
        .build();
    User savedUser2 = userRepository.save(user2);

    Category category = Category.builder()
        .name("Sport")
        .parent(null)
        .build();

    Category savedCategory = categoryRepository.save(category);

    Item item1 = Item.builder()
        .title("Title")
        .description("Description")
        .condition(ItemCondition.NEW)
        .state(ItemState.GIVEN)
        .city("City")
        .ownerVisibility(UserVisibility.BANNED)
        .categoryId(savedCategory.getId())
        .ownerId(savedUser1.getId())
        .build();

    Item savedItem1 = itemRepository.save(item1);

    Offer offer1 = Offer.builder()
        .buyerId(savedUser2.getId())
        .itemId(savedItem1.getId())
        .buyerVisibility(UserVisibility.BANNED)
        .status(OfferStatus.ACCEPTED)
        .build();

    Offer savedOffer1 = offerRepository.save(offer1);

    Review review1 = Review.builder()
        .comment("Comment 1")
        .offerId(savedOffer1.getId())
        .rating(5)
        .revieweeId(savedItem1.getOwnerId())
        .reviewerId(savedUser2.getId())
        .reviewerVisibility(UserVisibility.VISIBLE)
        .build();

    Review savedReview = reviewRepository.save(review1);

    reviewRepository.updateReviewerVisibility(List.of(savedUser2.getId()), UserVisibility.BANNED);

    Optional<Review> updatedReview = reviewRepository.findById(savedReview.getId());

    assertThat(updatedReview.isPresent()).isTrue();
    assertThat(updatedReview.get().getReviewerVisibility()).isEqualTo(UserVisibility.BANNED);

  }

}
