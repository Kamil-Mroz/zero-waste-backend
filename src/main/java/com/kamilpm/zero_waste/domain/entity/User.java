package com.kamilpm.zero_waste.domain.entity;

import java.util.Set;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
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
@Table(name = "users")
public class User {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(name = "first_name", nullable = false)
  private String firstName;

  @Column(name = "last_name", nullable = false)
  private String lastName;

  @Column(name = "email", nullable = false, unique = true)
  @Email
  private String email;

  @Column(name = "password", nullable = false)
  private String password;

  @Column(name = "phone_number", nullable = false)
  private String phoneNumber;

  @Column(name = "location", nullable = false)
  private String location;

  @Column(name = "is_banned")
  private Boolean isBanned;

  @Column(name = "roles", nullable = false)
  @Enumerated(EnumType.STRING)
  private Set<UserRole> roles;

}
