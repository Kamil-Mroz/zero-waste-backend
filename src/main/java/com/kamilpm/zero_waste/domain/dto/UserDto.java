package com.kamilpm.zero_waste.domain.dto;

import java.util.Set;
import java.util.UUID;

import com.kamilpm.zero_waste.domain.entity.UserRole;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDto {
  private UUID id;
  private String firstName;
  private String lastName;
  private String email;
  private String phoneNumber;
  private String city;
  private Set<UserRole> roles;

}
