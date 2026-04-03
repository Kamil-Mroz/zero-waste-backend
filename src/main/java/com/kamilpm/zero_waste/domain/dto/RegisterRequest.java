package com.kamilpm.zero_waste.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegisterRequest {
  private String firstName;
  private String lastName;
  private String email;
  private String password;
  private String location;
  private String phoneNumber;

}
