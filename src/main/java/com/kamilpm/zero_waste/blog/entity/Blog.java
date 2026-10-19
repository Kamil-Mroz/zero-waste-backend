package com.kamilpm.zero_waste.blog.entity;

import java.util.UUID;

import com.kamilpm.zero_waste.common.dto.UserVisibility;
import com.kamilpm.zero_waste.common.entity.ModeratableEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "blogs")
public class Blog extends ModeratableEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(name = "id", nullable = false, updatable = false)
  private UUID id;

  @Column(name = "title", nullable = false)
  private String title;

  @Column(name = "description", nullable = false)
  private String description;

  @Column(name = "content", nullable = false, columnDefinition = "TEXT")
  private String content;

  @Column(name = "author_id", nullable = true)
  private UUID authorId;

  @Column(name = "author_visibility", nullable = false)
  @Enumerated(EnumType.STRING)
  private UserVisibility authorVisibility;

  public Blog() {
  }

  public Blog(UUID id, String title, String description, String content, UUID authorId,
      UserVisibility authorVisibility) {
    this.id = id;
    this.title = title;
    this.description = description;
    this.content = content;
    this.authorId = authorId;
    this.authorVisibility = authorVisibility;
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

  public String getContent() {
    return content;
  }

  public void setContent(String content) {
    this.content = content;
  }

  public UUID getAuthorId() {
    return authorId;
  }

  public void setAuthorId(UUID authorId) {
    this.authorId = authorId;
  }

  public UserVisibility getAuthorVisibility() {
    return authorVisibility;
  }

  public void setAuthorVisibility(UserVisibility authorVisibility) {
    this.authorVisibility = authorVisibility;
  }

  @Override
  public String toString() {
    return "Blog [id=" + id + ", title=" + title + ", description=" + description + ", content=" + content
        + ", authorId=" + authorId + ", authorVisibility=" + authorVisibility + "]";
  }

}
