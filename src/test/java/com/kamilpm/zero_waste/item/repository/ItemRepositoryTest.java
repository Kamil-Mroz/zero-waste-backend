package com.kamilpm.zero_waste.item.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import com.kamilpm.zero_waste.category.entity.Category;
import com.kamilpm.zero_waste.category.repository.CategoryRepository;
import com.kamilpm.zero_waste.common.dto.ItemCondition;
import com.kamilpm.zero_waste.common.dto.ItemState;
import com.kamilpm.zero_waste.common.dto.UserRole;
import com.kamilpm.zero_waste.common.dto.UserVisibility;
import com.kamilpm.zero_waste.common.entity.ModerationStatus;
import com.kamilpm.zero_waste.config.PostgresTestConfiguration;
import com.kamilpm.zero_waste.image.entity.Image;
import com.kamilpm.zero_waste.image.repository.ImageRepository;
import com.kamilpm.zero_waste.item.entity.Item;
import com.kamilpm.zero_waste.user.entity.User;
import com.kamilpm.zero_waste.user.repository.UserRepository;

@DataJpaTest
@Import(PostgresTestConfiguration.class)
public class ItemRepositoryTest {

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private CategoryRepository categoryRepository;

  @Autowired
  private ItemRepository itemRepository;

  @Autowired
  private ImageRepository imageRepository;

  @Test
  void testCountTotalItemsByOwnerIdAndState() {

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

    Item item1 = Item.builder()
        .title("Title")
        .description("Description")
        .condition(ItemCondition.NEW)
        .state(ItemState.AVAILABLE)
        .city("City")
        .ownerVisibility(UserVisibility.VISIBLE)
        .categoryId(savedCategory.getId())
        .ownerId(savedUser.getId())
        .build();

    Item item2 = Item.builder()
        .title("Title2")
        .description("Description2")
        .condition(ItemCondition.NEW)
        .state(ItemState.GIVEN)
        .city("City2")
        .ownerVisibility(UserVisibility.VISIBLE)
        .categoryId(savedCategory.getId())
        .ownerId(savedUser.getId())
        .build();

    itemRepository.saveAll(List.of(item1, item2));

    long given = 0, available = 0, pending = 0;
    for (var row : itemRepository.countTotalItemsByOwnerIdAndState(savedUser.getId())) {
      switch (row.getItemState()) {
        case GIVEN -> given = row.getTotalItem();
        case AVAILABLE -> available = row.getTotalItem();
        case PENDING -> pending = row.getTotalItem();
      }
    }

    assertThat(given).isEqualTo(1);
    assertThat(available).isEqualTo(1);
    assertThat(pending).isEqualTo(0);

  }

  @Test
  void testDeleteAllByOwnerIds() {

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

    Item item1 = Item.builder()
        .title("Title")
        .description("Description")
        .condition(ItemCondition.NEW)
        .state(ItemState.AVAILABLE)
        .city("City")
        .ownerVisibility(UserVisibility.VISIBLE)
        .categoryId(savedCategory.getId())
        .ownerId(savedUser.getId())
        .build();

    Item item2 = Item.builder()
        .title("Title2")
        .description("Description2")
        .condition(ItemCondition.NEW)
        .state(ItemState.GIVEN)
        .city("City2")
        .ownerVisibility(UserVisibility.VISIBLE)
        .categoryId(savedCategory.getId())
        .ownerId(savedUser.getId())
        .build();

    itemRepository.saveAll(List.of(item1, item2));

    itemRepository.deleteAllByOwnerIds(List.of(savedUser.getId()));

    List<Item> items = itemRepository.findByOwnerId(savedUser.getId());

    assertThat(items).isEmpty();

  }

  @Test
  void testExistsByCategoryId() {

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

    Item item1 = Item.builder()
        .title("Title")
        .description("Description")
        .condition(ItemCondition.NEW)
        .state(ItemState.AVAILABLE)
        .city("City")
        .ownerVisibility(UserVisibility.VISIBLE)
        .categoryId(savedCategory.getId())
        .ownerId(savedUser.getId())
        .build();
    itemRepository.save(item1);

    boolean exists = itemRepository.existsByCategoryId(savedCategory.getId());

    assertThat(exists).isTrue();

  }

  @Test
  void testFindImageIdsByOwnerIds() {

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

    User user2 = User.builder()
        .nickname("test-123")
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
        .state(ItemState.AVAILABLE)
        .city("City")
        .ownerVisibility(UserVisibility.VISIBLE)
        .categoryId(savedCategory.getId())
        .ownerId(savedUser.getId())
        .build();

    Item item2 = Item.builder()
        .title("Title2")
        .description("Description2")
        .condition(ItemCondition.NEW)
        .state(ItemState.GIVEN)
        .city("City2")
        .ownerVisibility(UserVisibility.VISIBLE)
        .categoryId(savedCategory.getId())
        .ownerId(savedUser2.getId())
        .build();

    List<Item> items = itemRepository.saveAllAndFlush(List.of(item1, item2));

    Instant now = Instant.now();

    Image image1 = Image.builder()
        .itemId(items.get(0).getId())
        .originalName("OriginalName")
        .storedName("path")
        .mimeType("image/png")
        .size(441L)
        .createdAt(now)
        .build();

    Image image2 = Image.builder()
        .itemId(items.get(0).getId())
        .originalName("OriginalName")
        .storedName("path")
        .mimeType("image/png")
        .size(441L)
        .createdAt(now)
        .build();

    Image image3 = Image.builder()
        .itemId(items.get(1).getId())
        .originalName("OriginalName")
        .storedName("path")
        .mimeType("image/png")
        .size(441L)
        .createdAt(now)
        .build();

    List<Image> images = imageRepository.saveAllAndFlush(List.of(image1, image2, image3));

    Item savedItem1 = items.get(0);
    Item savedItem2 = items.get(1);

    savedItem1
        .setImageIds(
            new ArrayList<>(images.stream().filter(image -> Objects.equals(image.getItemId(), savedItem1.getId()))
                .map(image -> image.getId()).toList()));

    savedItem2
        .setImageIds(
            new ArrayList<>(images.stream().filter(image -> Objects.equals(image.getItemId(), savedItem2.getId()))
                .map(image -> image.getId()).toList()));

    itemRepository.saveAllAndFlush(List.of(savedItem1, savedItem2));

    List<UUID> imageIds = itemRepository.findImageIdsByOwnerIds(List.of(savedUser.getId()));

    assertThat(imageIds.size()).isEqualTo(2);

  }

  @Test
  void testFindOwnItems() {

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

    Category category1 = Category.builder()
        .name("Sport")
        .parent(null)
        .build();
    Category savedCategory1 = categoryRepository.save(category1);

    Category category2 = Category.builder()
        .name("Home")
        .parent(null)
        .build();
    Category savedCategory2 = categoryRepository.save(category2);

    Item item1 = Item.builder()
        .title("Title")
        .description("Description")
        .condition(ItemCondition.NEW)
        .state(ItemState.AVAILABLE)
        .city("City")
        .ownerVisibility(UserVisibility.VISIBLE)
        .categoryId(savedCategory1.getId())
        .ownerId(savedUser.getId())
        .build();

    Item item2 = Item.builder()
        .title("Title2")
        .description("Description2")
        .condition(ItemCondition.NEW)
        .state(ItemState.AVAILABLE)
        .city("City2")
        .ownerVisibility(UserVisibility.VISIBLE)
        .categoryId(savedCategory2.getId())
        .ownerId(savedUser.getId())
        .build();

    itemRepository.saveAll(List.of(item1, item2));

    Page<Item> items = itemRepository.findOwnItems(savedUser.getId(), null,

        null,
        List.of(ItemState.AVAILABLE, ItemState.PENDING), PageRequest.of(0, 20));

    assertThat(items.getTotalElements()).isEqualTo(2L);
    assertThat(items.getContent().stream().anyMatch((item) -> Objects.equals(item.getTitle(), "Title"))).isTrue();

  }

  @Test
  void testSearchItems() {

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

    Category category1 = Category.builder()
        .name("Sport")
        .parent(null)
        .build();
    Category savedCategory1 = categoryRepository.save(category1);

    Category category2 = Category.builder()
        .name("Home")
        .parent(null)
        .build();
    Category savedCategory2 = categoryRepository.save(category2);

    Item item1 = Item.builder()
        .title("Title")
        .description("Description")
        .condition(ItemCondition.NEW)
        .state(ItemState.AVAILABLE)
        .city("City")
        .ownerVisibility(UserVisibility.VISIBLE)
        .categoryId(savedCategory1.getId())
        .ownerId(savedUser.getId())
        .build();

    Item item2 = Item.builder()
        .title("Title2")
        .description("Description2")
        .condition(ItemCondition.NEW)
        .state(ItemState.AVAILABLE)
        .city("City2")
        .ownerVisibility(UserVisibility.VISIBLE)
        .categoryId(savedCategory2.getId())
        .ownerId(savedUser.getId())
        .build();

    itemRepository.saveAll(List.of(item1, item2));

    Page<Item> items = itemRepository.searchItems(null, ItemState.AVAILABLE, null, ModerationStatus.VISIBLE,
        Set.of(savedCategory2.getId()),
        UserVisibility.VISIBLE, PageRequest.of(0, 20));

    assertThat(items.getTotalElements()).isEqualTo(1L);
    assertThat(items.getContent().getFirst().getTitle()).isEqualTo("Title2");

  }

  @Test
  void testUpdateItemState() {

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

    Item item1 = Item.builder()
        .title("Title")
        .description("Description")
        .condition(ItemCondition.NEW)
        .state(ItemState.AVAILABLE)
        .city("City")
        .ownerVisibility(UserVisibility.VISIBLE)
        .categoryId(savedCategory.getId())
        .ownerId(savedUser.getId())
        .build();
    Item savedItem = itemRepository.save(item1);

    itemRepository.updateItemState(savedItem.getId(), ItemState.PENDING);

    Optional<Item> updatedItem = itemRepository.findById(savedItem.getId());

    assertThat(updatedItem).isPresent();
    assertThat(updatedItem.get().getState()).isEqualTo(ItemState.PENDING);

  }

  @Test
  void testUpdateOwnerVisibility() {

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

    Item item1 = Item.builder()
        .title("Title")
        .description("Description")
        .condition(ItemCondition.NEW)
        .state(ItemState.AVAILABLE)
        .city("City")
        .ownerVisibility(UserVisibility.VISIBLE)
        .categoryId(savedCategory.getId())
        .ownerId(savedUser.getId())
        .build();
    Item savedItem = itemRepository.save(item1);

    itemRepository.updateOwnerVisibility(List.of(savedItem.getOwnerId()), UserVisibility.BANNED, ItemState.GIVEN);

    Optional<Item> updatedItem = itemRepository.findById(savedItem.getId());

    assertThat(updatedItem).isPresent();
    assertThat(updatedItem.get().getOwnerVisibility()).isEqualTo(UserVisibility.BANNED);

  }
}
