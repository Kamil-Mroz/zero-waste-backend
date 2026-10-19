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
    User user = new User();
    user.setNickname("test-123");
    user.setEmail(email);
    user.setPassword(null);
    user.setRole(UserRole.USER);
    user.setBanActive(false);
    user.setBannedUntil(null);
    User savedUser = userRepository.save(user);

    Category category = new Category();
    category.setName("Sport");
    category.setParent(null);
    Category savedCategory = categoryRepository.save(category);

    Item item1 = new Item();
    item1.setTitle("Title");
    item1.setDescription("Description");
    item1.setCondition(ItemCondition.NEW);
    item1.setState(ItemState.AVAILABLE);
    item1.setCity("City");
    item1.setOwnerVisibility(UserVisibility.VISIBLE);
    item1.setCategoryId(savedCategory.getId());
    item1.setOwnerId(savedUser.getId());

    Item item2 = new Item();
    item2.setTitle("Title2");
    item2.setDescription("Description2");
    item2.setCondition(ItemCondition.NEW);
    item2.setState(ItemState.GIVEN);
    item2.setCity("City2");
    item2.setOwnerVisibility(UserVisibility.VISIBLE);
    item2.setCategoryId(savedCategory.getId());
    item2.setOwnerId(savedUser.getId());

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
    User user = new User();
    user.setNickname("test-123");
    user.setEmail(email);
    user.setPassword(null);
    user.setRole(UserRole.USER);
    user.setBanActive(false);
    user.setBannedUntil(null);
    User savedUser = userRepository.save(user);

    Category category = new Category();
    category.setName("Sport");
    category.setParent(null);
    Category savedCategory = categoryRepository.save(category);

    Item item1 = new Item();
    item1.setTitle("Title");
    item1.setDescription("Description");
    item1.setCondition(ItemCondition.NEW);
    item1.setState(ItemState.AVAILABLE);
    item1.setCity("City");
    item1.setOwnerVisibility(UserVisibility.VISIBLE);
    item1.setCategoryId(savedCategory.getId());
    item1.setOwnerId(savedUser.getId());

    Item item2 = new Item();
    item2.setTitle("Title2");
    item2.setDescription("Description2");
    item2.setCondition(ItemCondition.NEW);
    item2.setState(ItemState.GIVEN);
    item2.setCity("City2");
    item2.setOwnerVisibility(UserVisibility.VISIBLE);
    item2.setCategoryId(savedCategory.getId());
    item2.setOwnerId(savedUser.getId());

    itemRepository.saveAll(List.of(item1, item2));

    itemRepository.deleteAllByOwnerIds(List.of(savedUser.getId()));

    List<Item> items = itemRepository.findByOwnerId(savedUser.getId());

    assertThat(items).isEmpty();

  }

  @Test
  void testExistsByCategoryId() {

    String email = "test@example.com";
    User user = new User();
    user.setNickname("test-123");
    user.setEmail(email);
    user.setPassword(null);
    user.setRole(UserRole.USER);
    user.setBanActive(false);
    user.setBannedUntil(null);
    User savedUser = userRepository.save(user);

    Category category = new Category();
    category.setName("Sport");
    category.setParent(null);
    Category savedCategory = categoryRepository.save(category);

    Item item1 = new Item();
    item1.setTitle("Title");
    item1.setDescription("Description");
    item1.setCondition(ItemCondition.NEW);
    item1.setState(ItemState.AVAILABLE);
    item1.setCity("City");
    item1.setOwnerVisibility(UserVisibility.VISIBLE);
    item1.setCategoryId(savedCategory.getId());
    item1.setOwnerId(savedUser.getId());
    itemRepository.save(item1);

    boolean exists = itemRepository.existsByCategoryId(savedCategory.getId());

    assertThat(exists).isTrue();

  }

  @Test
  void testFindImageIdsByOwnerIds() {

    String email = "test@example.com";
    User user = new User();
    user.setNickname("test-123");
    user.setEmail(email);
    user.setPassword(null);
    user.setRole(UserRole.USER);
    user.setBanActive(false);
    user.setBannedUntil(null);
    User savedUser = userRepository.save(user);

    User user2 = new User();
    user2.setNickname("test-123");
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
    item1.setState(ItemState.AVAILABLE);
    item1.setCity("City");
    item1.setOwnerVisibility(UserVisibility.VISIBLE);
    item1.setCategoryId(savedCategory.getId());
    item1.setOwnerId(savedUser.getId());

    Item item2 = new Item();
    item2.setTitle("Title2");
    item2.setDescription("Description2");
    item2.setCondition(ItemCondition.NEW);
    item2.setState(ItemState.GIVEN);
    item2.setCity("City2");
    item2.setOwnerVisibility(UserVisibility.VISIBLE);
    item2.setCategoryId(savedCategory.getId());
    item2.setOwnerId(savedUser2.getId());

    List<Item> items = itemRepository.saveAllAndFlush(List.of(item1, item2));

    Instant now = Instant.now();

    Image image1 = new Image();
    image1.setItemId(items.get(0).getId());
    image1.setOriginalName("OriginalName");
    image1.setStoredName("path");
    image1.setMimeType("image/png");
    image1.setSize(441L);
    image1.setCreatedAt(now);

    Image image2 = new Image();
    image2.setItemId(items.get(0).getId());
    image2.setOriginalName("OriginalName");
    image2.setStoredName("path");
    image2.setMimeType("image/png");
    image2.setSize(441L);
    image2.setCreatedAt(now);

    Image image3 = new Image();
    image3.setItemId(items.get(1).getId());
    image3.setOriginalName("OriginalName");
    image3.setStoredName("path");
    image3.setMimeType("image/png");
    image3.setSize(441L);
    image3.setCreatedAt(now);

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
    User user = new User();
    user.setNickname("test-123");
    user.setEmail(email);
    user.setPassword(null);
    user.setRole(UserRole.USER);
    user.setBanActive(false);
    user.setBannedUntil(null);
    User savedUser = userRepository.save(user);

    Category category1 = new Category();
    category1.setName("Sport");
    category1.setParent(null);
    Category savedCategory1 = categoryRepository.save(category1);

    Category category2 = new Category();
    category2.setName("Home");
    category2.setParent(null);
    Category savedCategory2 = categoryRepository.save(category2);

    Item item1 = new Item();
    item1.setTitle("Title");
    item1.setDescription("Description");
    item1.setCondition(ItemCondition.NEW);
    item1.setState(ItemState.AVAILABLE);
    item1.setCity("City");
    item1.setOwnerVisibility(UserVisibility.VISIBLE);
    item1.setCategoryId(savedCategory1.getId());
    item1.setOwnerId(savedUser.getId());

    Item item2 = new Item();
    item2.setTitle("Title2");
    item2.setDescription("Description2");
    item2.setCondition(ItemCondition.NEW);
    item2.setState(ItemState.AVAILABLE);
    item2.setCity("City2");
    item2.setOwnerVisibility(UserVisibility.VISIBLE);
    item2.setCategoryId(savedCategory2.getId());
    item2.setOwnerId(savedUser.getId());

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
    User user = new User();
    user.setNickname("test-123");
    user.setEmail(email);
    user.setPassword(null);
    user.setRole(UserRole.USER);
    user.setBanActive(false);
    user.setBannedUntil(null);
    User savedUser = userRepository.save(user);

    Category category1 = new Category();
    category1.setName("Sport");
    category1.setParent(null);
    Category savedCategory1 = categoryRepository.save(category1);

    Category category2 = new Category();
    category2.setName("Home");
    category2.setParent(null);
    Category savedCategory2 = categoryRepository.save(category2);

    Item item1 = new Item();
    item1.setTitle("Title");
    item1.setDescription("Description");
    item1.setCondition(ItemCondition.NEW);
    item1.setState(ItemState.AVAILABLE);
    item1.setCity("City");
    item1.setOwnerVisibility(UserVisibility.VISIBLE);
    item1.setCategoryId(savedCategory1.getId());
    item1.setOwnerId(savedUser.getId());

    Item item2 = new Item();
    item2.setTitle("Title2");
    item2.setDescription("Description2");
    item2.setCondition(ItemCondition.NEW);
    item2.setState(ItemState.AVAILABLE);
    item2.setCity("City2");
    item2.setOwnerVisibility(UserVisibility.VISIBLE);
    item2.setCategoryId(savedCategory2.getId());
    item2.setOwnerId(savedUser.getId());

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
    User user = new User();
    user.setNickname("test-123");
    user.setEmail(email);
    user.setPassword(null);
    user.setRole(UserRole.USER);
    user.setBanActive(false);
    user.setBannedUntil(null);
    User savedUser = userRepository.save(user);

    Category category = new Category();
    category.setName("Sport");
    category.setParent(null);
    Category savedCategory = categoryRepository.save(category);

    Item item1 = new Item();
    item1.setTitle("Title");
    item1.setDescription("Description");
    item1.setCondition(ItemCondition.NEW);
    item1.setState(ItemState.AVAILABLE);
    item1.setCity("City");
    item1.setOwnerVisibility(UserVisibility.VISIBLE);
    item1.setCategoryId(savedCategory.getId());
    item1.setOwnerId(savedUser.getId());
    Item savedItem = itemRepository.save(item1);

    itemRepository.updateItemState(savedItem.getId(), ItemState.PENDING);

    Optional<Item> updatedItem = itemRepository.findById(savedItem.getId());

    assertThat(updatedItem).isPresent();
    assertThat(updatedItem.get().getState()).isEqualTo(ItemState.PENDING);

  }

  @Test
  void testUpdateOwnerVisibility() {

    String email = "test@example.com";
    User user = new User();
    user.setNickname("test-123");
    user.setEmail(email);
    user.setPassword(null);
    user.setRole(UserRole.USER);
    user.setBanActive(false);
    user.setBannedUntil(null);
    User savedUser = userRepository.save(user);

    Category category = new Category();
    category.setName("Sport");
    category.setParent(null);
    Category savedCategory = categoryRepository.save(category);

    Item item1 = new Item();
    item1.setTitle("Title");
    item1.setDescription("Description");
    item1.setCondition(ItemCondition.NEW);
    item1.setState(ItemState.AVAILABLE);
    item1.setCity("City");
    item1.setOwnerVisibility(UserVisibility.VISIBLE);
    item1.setCategoryId(savedCategory.getId());
    item1.setOwnerId(savedUser.getId());
    Item savedItem = itemRepository.save(item1);

    itemRepository.updateOwnerVisibility(List.of(savedItem.getOwnerId()), UserVisibility.BANNED, ItemState.GIVEN);

    Optional<Item> updatedItem = itemRepository.findById(savedItem.getId());

    assertThat(updatedItem).isPresent();
    assertThat(updatedItem.get().getOwnerVisibility()).isEqualTo(UserVisibility.BANNED);

  }
}
