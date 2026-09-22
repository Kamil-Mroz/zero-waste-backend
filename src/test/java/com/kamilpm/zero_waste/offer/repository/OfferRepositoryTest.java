package com.kamilpm.zero_waste.offer.repository;

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
import com.kamilpm.zero_waste.user.entity.User;
import com.kamilpm.zero_waste.user.repository.UserRepository;

@DataJpaTest
@Import(PostgresTestConfiguration.class)
public class OfferRepositoryTest {

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private CategoryRepository categoryRepository;

  @Autowired
  private ItemRepository itemRepository;

  @Autowired
  private OfferRepository offerRepository;

  @Test
  void testUpdateBuyerVisibility() {

    String email = "test@example.com";
    User user = User.builder()
        .nickname("test-123")
        .email(email)
        .password(null)
        .role(UserRole.USER)
        .banActive(false)
        .bannedUntil(null)
        .build();
    User savedUser = userRepository.save(user);

    Category category = Category.builder()
        .name("Sport")
        .parent(null)
        .build();

    Category savedCategory = categoryRepository.save(category);

    Item item = Item.builder()
        .title("Title")
        .description("Description")
        .condition(ItemCondition.NEW)
        .state(ItemState.AVAILABLE)
        .city("City")
        .ownerVisibility(UserVisibility.VISIBLE)
        .categoryId(savedCategory.getId())
        .ownerId(savedUser.getId())
        .build();

    Item savedItem = itemRepository.save(item);

    Offer offer = Offer.builder()
        .buyerId(savedUser.getId())
        .itemId(savedItem.getId())
        .buyerVisibility(UserVisibility.VISIBLE)
        .status(OfferStatus.PENDING)
        .build();
    Offer savedOffer = offerRepository.save(offer);

    offerRepository.updateBuyerVisibility(savedUser.getId(), UserVisibility.BANNED, OfferStatus.PENDING);

    Optional<Offer> updatedOffer = offerRepository.findById(savedOffer.getId());

    assertThat(updatedOffer.isPresent()).isTrue();
    assertThat(updatedOffer.get().getBuyerVisibility()).isEqualTo(UserVisibility.BANNED);

  }

  @Test
  void testUpdateBuyerVisibility2() {

    String email = "test@example.com";
    User user = User.builder()
        .nickname("test-123")
        .email(email)
        .password(null)
        .role(UserRole.USER)
        .banActive(false)
        .bannedUntil(null)
        .build();
    User savedUser = userRepository.save(user);

    Category category = Category.builder()
        .name("Sport")
        .parent(null)
        .build();

    Category savedCategory = categoryRepository.save(category);

    Item item = Item.builder()
        .title("Title")
        .description("Description")
        .condition(ItemCondition.NEW)
        .state(ItemState.AVAILABLE)
        .city("City")
        .ownerVisibility(UserVisibility.VISIBLE)
        .categoryId(savedCategory.getId())
        .ownerId(savedUser.getId())
        .build();

    Item savedItem = itemRepository.save(item);

    Offer offer = Offer.builder()
        .buyerId(savedUser.getId())
        .itemId(savedItem.getId())
        .buyerVisibility(UserVisibility.VISIBLE)
        .status(OfferStatus.PENDING)
        .build();
    Offer savedOffer = offerRepository.save(offer);

    offerRepository.updateBuyerVisibility(List.of(savedUser.getId()), UserVisibility.BANNED, OfferStatus.PENDING);

    Optional<Offer> updatedOffer = offerRepository.findById(savedOffer.getId());

    assertThat(updatedOffer.isPresent()).isTrue();
    assertThat(updatedOffer.get().getBuyerVisibility()).isEqualTo(UserVisibility.BANNED);

  }
}
