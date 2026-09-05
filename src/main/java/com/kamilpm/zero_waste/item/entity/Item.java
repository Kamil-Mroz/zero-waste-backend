package com.kamilpm.zero_waste.item.entity;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.kamilpm.zero_waste.common.entity.ModeratableEntity;
import com.kamilpm.zero_waste.item.api.ItemState;

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
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Builder
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

  @Column(name = "category_id", nullable = false)
  private UUID categoryId;

  @Column(name = "owner_id", nullable = false)
  private UUID ownerId;

  @ElementCollection
  @CollectionTable(name = "item_images", joinColumns = @JoinColumn(name = "item_id"))
  @OrderColumn(name = "image_order")
  @Column(name = "image_id")
  @Builder.Default
  private List<UUID> imageIds = new ArrayList<>();

  @Column(name = "thumbnail_id")
  private UUID thumbnailId;

}
