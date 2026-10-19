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
    User user1 = new User();
    user1.setNickname("test-123");
    user1.setEmail(email);
    user1.setPassword(null);
    user1.setRole(UserRole.USER);
    user1.setBanActive(false);
    user1.setBannedUntil(null);
    User savedUser1 = userRepository.save(user1);

    User user2 = new User();
    user2.setNickname("test2-123");
    user2.setEmail("2" + email);
    user2.setPassword(null);
    user2.setRole(UserRole.USER);
    user2.setBanActive(false);
    user2.setBannedUntil(null);
    User savedUser2 = userRepository.save(user2);

    Category category = new Category();
    category.setName("Sport");
    category.setParent(null);

    Category savedCategory = categoryRepository.save(category);

    Item item1 = new Item();
    item1.setTitle("Title");
    item1.setDescription("Description");
    item1.setCondition(ItemCondition.NEW);
    item1.setState(ItemState.GIVEN);
    item1.setCity("City");
    item1.setOwnerVisibility(UserVisibility.VISIBLE);
    item1.setCategoryId(savedCategory.getId());
    item1.setOwnerId(savedUser1.getId());

    Item savedItem1 = itemRepository.save(item1);

    Item item2 = new Item();
    item2.setTitle("Title");
    item2.setDescription("Description");
    item2.setCondition(ItemCondition.NEW);
    item2.setState(ItemState.GIVEN);
    item2.setCity("City");
    item2.setOwnerVisibility(UserVisibility.VISIBLE);
    item2.setCategoryId(savedCategory.getId());
    item2.setOwnerId(savedUser1.getId());

    Item savedItem2 = itemRepository.save(item2);

    Offer offer1 = new Offer();
    offer1.setBuyerId(savedUser2.getId());
    offer1.setItemId(savedItem1.getId());
    offer1.setBuyerVisibility(UserVisibility.VISIBLE);
    offer1.setStatus(OfferStatus.ACCEPTED);

    Offer savedOffer1 = offerRepository.save(offer1);

    Offer offer2 = new Offer();
    offer2.setBuyerId(savedUser2.getId());
    offer2.setItemId(savedItem2.getId());
    offer2.setBuyerVisibility(UserVisibility.VISIBLE);
    offer2.setStatus(OfferStatus.ACCEPTED);
    Offer savedOffer2 = offerRepository.save(offer2);

    Review review1 = new Review();
    review1.setComment("Comment 1");
    review1.setOfferId(savedOffer1.getId());
    review1.setRating(5);
    review1.setRevieweeId(savedItem1.getOwnerId());
    review1.setReviewerId(savedUser2.getId());
    review1.setReviewerVisibility(UserVisibility.VISIBLE);

    reviewRepository.save(review1);

    Review review2 = new Review();
    review2.setComment("Comment 1");
    review2.setOfferId(savedOffer2.getId());
    review2.setRating(2);
    review2.setRevieweeId(savedItem2.getOwnerId());
    review2.setReviewerId(savedUser2.getId());
    review2.setReviewerVisibility(UserVisibility.VISIBLE);

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
    User user1 = new User();
    user1.setNickname("test-123");
    user1.setEmail(email);
    user1.setPassword(null);
    user1.setRole(UserRole.USER);
    user1.setBanActive(false);
    user1.setBannedUntil(null);
    User savedUser1 = userRepository.save(user1);

    User user2 = new User();
    user2.setNickname("test2-123");
    user2.setEmail("2" + email);
    user2.setPassword(null);
    user2.setRole(UserRole.USER);
    user2.setBanActive(false);
    user2.setBannedUntil(null);
    User savedUser2 = userRepository.save(user2);

    Category category = new Category();
    category.setName("Sport");
    category.setParent(null);

    Category savedCategory = categoryRepository.save(category);

    Item item1 = new Item();
    item1.setTitle("Title");
    item1.setDescription("Description");
    item1.setCondition(ItemCondition.NEW);
    item1.setState(ItemState.GIVEN);
    item1.setCity("City");
    item1.setOwnerVisibility(UserVisibility.VISIBLE);
    item1.setCategoryId(savedCategory.getId());
    item1.setOwnerId(savedUser1.getId());

    Item savedItem1 = itemRepository.save(item1);

    Offer offer1 = new Offer();
    offer1.setBuyerId(savedUser2.getId());
    offer1.setItemId(savedItem1.getId());
    offer1.setBuyerVisibility(UserVisibility.VISIBLE);
    offer1.setStatus(OfferStatus.ACCEPTED);

    Offer savedOffer1 = offerRepository.save(offer1);

    Review review1 = new Review();
    review1.setComment("Comment 1");
    review1.setOfferId(savedOffer1.getId());
    review1.setRating(5);
    review1.setRevieweeId(savedItem1.getOwnerId());
    review1.setReviewerId(savedUser2.getId());
    review1.setReviewerVisibility(UserVisibility.VISIBLE);

    Review savedReview = reviewRepository.save(review1);

    Boolean isReviewerOrReviewee = reviewRepository.isReviewerOrReviewee(savedReview.getId(), savedUser1.getId());

    assertThat(isReviewerOrReviewee).isTrue();

  }

  @Test
  void testUpdateReviewerVisibility() {
    String email = "test@example.com";
    User user1 = new User();
    user1.setNickname("test-123");
    user1.setEmail(email);
    user1.setPassword(null);
    user1.setRole(UserRole.USER);
    user1.setBanActive(false);
    user1.setBannedUntil(null);
    User savedUser1 = userRepository.save(user1);

    User user2 = new User();
    user2.setNickname("test2-123");
    user2.setEmail("2" + email);
    user2.setPassword(null);
    user2.setRole(UserRole.USER);
    user2.setBanActive(true);
    user2.setBannedUntil(null);
    User savedUser2 = userRepository.save(user2);

    Category category = new Category();
    category.setName("Sport");
    category.setParent(null);

    Category savedCategory = categoryRepository.save(category);

    Item item1 = new Item();
    item1.setTitle("Title");
    item1.setDescription("Description");
    item1.setCondition(ItemCondition.NEW);
    item1.setState(ItemState.GIVEN);
    item1.setCity("City");
    item1.setOwnerVisibility(UserVisibility.BANNED);
    item1.setCategoryId(savedCategory.getId());
    item1.setOwnerId(savedUser1.getId());

    Item savedItem1 = itemRepository.save(item1);

    Offer offer1 = new Offer();
    offer1.setBuyerId(savedUser2.getId());
    offer1.setItemId(savedItem1.getId());
    offer1.setBuyerVisibility(UserVisibility.BANNED);
    offer1.setStatus(OfferStatus.ACCEPTED);

    Offer savedOffer1 = offerRepository.save(offer1);

    Review review1 = new Review();
    review1.setComment("Comment 1");
    review1.setOfferId(savedOffer1.getId());
    review1.setRating(5);
    review1.setRevieweeId(savedItem1.getOwnerId());
    review1.setReviewerId(savedUser2.getId());
    review1.setReviewerVisibility(UserVisibility.VISIBLE);

    Review savedReview = reviewRepository.save(review1);

    reviewRepository.updateReviewerVisibility(savedUser2.getId(), UserVisibility.BANNED);

    Optional<Review> updatedReview = reviewRepository.findById(savedReview.getId());

    assertThat(updatedReview.isPresent()).isTrue();
    assertThat(updatedReview.get().getReviewerVisibility()).isEqualTo(UserVisibility.BANNED);

  }

  @Test
  void testUpdateReviewerVisibility2() {

    String email = "test@example.com";
    User user1 = new User();
    user1.setNickname("test-123");
    user1.setEmail(email);
    user1.setPassword(null);
    user1.setRole(UserRole.USER);
    user1.setBanActive(false);
    user1.setBannedUntil(null);
    User savedUser1 = userRepository.save(user1);

    User user2 = new User();
    user2.setNickname("test2-123");
    user2.setEmail("2" + email);
    user2.setPassword(null);
    user2.setRole(UserRole.USER);
    user2.setBanActive(true);
    user2.setBannedUntil(null);
    User savedUser2 = userRepository.save(user2);

    Category category = new Category();
    category.setName("Sport");
    category.setParent(null);

    Category savedCategory = categoryRepository.save(category);

    Item item1 = new Item();
    item1.setTitle("Title");
    item1.setDescription("Description");
    item1.setCondition(ItemCondition.NEW);
    item1.setState(ItemState.GIVEN);
    item1.setCity("City");
    item1.setOwnerVisibility(UserVisibility.BANNED);
    item1.setCategoryId(savedCategory.getId());
    item1.setOwnerId(savedUser1.getId());

    Item savedItem1 = itemRepository.save(item1);

    Offer offer1 = new Offer();
    offer1.setBuyerId(savedUser2.getId());
    offer1.setItemId(savedItem1.getId());
    offer1.setBuyerVisibility(UserVisibility.BANNED);
    offer1.setStatus(OfferStatus.ACCEPTED);

    Offer savedOffer1 = offerRepository.save(offer1);

    Review review1 = new Review();
    review1.setComment("Comment 1");
    review1.setOfferId(savedOffer1.getId());
    review1.setRating(5);
    review1.setRevieweeId(savedItem1.getOwnerId());
    review1.setReviewerId(savedUser2.getId());
    review1.setReviewerVisibility(UserVisibility.VISIBLE);

    Review savedReview = reviewRepository.save(review1);

    reviewRepository.updateReviewerVisibility(List.of(savedUser2.getId()), UserVisibility.BANNED);

    Optional<Review> updatedReview = reviewRepository.findById(savedReview.getId());

    assertThat(updatedReview.isPresent()).isTrue();
    assertThat(updatedReview.get().getReviewerVisibility()).isEqualTo(UserVisibility.BANNED);

  }

}
