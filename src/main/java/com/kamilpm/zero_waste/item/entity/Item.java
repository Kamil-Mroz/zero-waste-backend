package com.kamilpm.zero_waste.item.entity;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.kamilpm.zero_waste.common.dto.ItemCondition;
import com.kamilpm.zero_waste.common.dto.ItemState;
import com.kamilpm.zero_waste.common.dto.UserVisibility;
import com.kamilpm.zero_waste.common.entity.ModeratableEntity;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;

@Entity
@Table(name = "items")
public class Item extends ModeratableEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(name = "id", nullable = false, updatable = false)
  private UUID id;

  @Column(name = "title", nullable = false)
  private String title;

  @Column(name = "description", nullable = false)
  private String description;

  @Column(name = "city", nullable = false)
  private String city;

  @Column(name = "condition", nullable = false)
  @Enumerated(EnumType.STRING)
  private ItemCondition condition;

  @Column(name = "state", nullable = false)
  @Enumerated(EnumType.STRING)
  private ItemState state;

  @Column(name = "owner_visibility", nullable = false)
  @Enumerated(EnumType.STRING)
  private UserVisibility ownerVisibility;

  @Column(name = "category_id", nullable = false)
  private UUID categoryId;

  @Column(name = "owner_id", nullable = false)
  private UUID ownerId;

  @ElementCollection
  @CollectionTable(name = "item_images", joinColumns = @JoinColumn(name = "item_id"))
  @OrderColumn(name = "image_order")
  @Column(name = "image_id")
  private List<UUID> imageIds = new ArrayList<>();

  @Column(name = "thumbnail_id")
  private UUID thumbnailId;

  public Item() {
  }

  public Item(UUID id, String title, String description, String city, ItemCondition condition, ItemState state,
      UserVisibility ownerVisibility, UUID categoryId, UUID ownerId, List<UUID> imageIds, UUID thumbnailId) {
    this.id = id;
    this.title = title;
    this.description = description;
    this.city = city;
    this.condition = condition;
    this.state = state;
    this.ownerVisibility = ownerVisibility;
    this.categoryId = categoryId;
    this.ownerId = ownerId;
    this.imageIds = imageIds;
    this.thumbnailId = thumbnailId;
  }

  public UUID getId() {
    return id;
  }

  public void setId(UUID id) {
    this.id = id;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getCity() {
    return city;
  }

  public void setCity(String city) {
    this.city = city;
  }

  public ItemCondition getCondition() {
    return condition;
  }

  public void setCondition(ItemCondition condition) {
    this.condition = condition;
  }

  public ItemState getState() {
    return state;
  }

  public void setState(ItemState state) {
    this.state = state;
  }

  public UserVisibility getOwnerVisibility() {
    return ownerVisibility;
  }

  public void setOwnerVisibility(UserVisibility ownerVisibility) {
    this.ownerVisibility = ownerVisibility;
  }

  public UUID getCategoryId() {
    return categoryId;
  }

  public void setCategoryId(UUID categoryId) {
    this.categoryId = categoryId;
  }

  public UUID getOwnerId() {
    return ownerId;
  }

  public void setOwnerId(UUID ownerId) {
    this.ownerId = ownerId;
  }

  public List<UUID> getImageIds() {
    return imageIds;
  }

  public void setImageIds(List<UUID> imageIds) {
    this.imageIds = imageIds;
  }

  public UUID getThumbnailId() {
    return thumbnailId;
  }

  public void setThumbnailId(UUID thumbnailId) {
    this.thumbnailId = thumbnailId;
  }

  @Override
  public String toString() {
    return "Item [id=" + id + ", title=" + title + ", description=" + description + ", city=" + city + ", condition="
        + condition + ", state=" + state + ", ownerVisibility=" + ownerVisibility + ", categoryId=" + categoryId
        + ", ownerId=" + ownerId + ", imageIds=" + imageIds + ", thumbnailId=" + thumbnailId + "]";
  }

}
