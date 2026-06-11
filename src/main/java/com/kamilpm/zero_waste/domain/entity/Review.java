package com.kamilpm.zero_waste.domain.entity;

import java.util.UUID;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
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
@Table(name = "reviews")
public class Review extends BaseEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(name = "id", nullable = false, updatable = false)
  private UUID id;

  @Column(name = "description", nullable = true)
  private String comment;

  @Column(name = "rating", nullable = false)
  private int rating;

  @OneToOne(cascade = CascadeType.ALL)
  @JoinColumn(name = "offer_id", referencedColumnName = "id")
  private Offer offer;

  @ManyToOne
  @JoinColumn(name = "reviewer_id")
  private User reviewer;

  @ManyToOne
  @JoinColumn(name = "reviewee_id")
  private User reviewee;

}
