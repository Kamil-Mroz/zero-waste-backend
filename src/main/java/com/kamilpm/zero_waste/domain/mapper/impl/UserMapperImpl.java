package com.kamilpm.zero_waste.domain.mapper.impl;

import org.springframework.stereotype.Component;

import com.kamilpm.zero_waste.domain.dto.UserDto;
import com.kamilpm.zero_waste.domain.entity.User;
import com.kamilpm.zero_waste.domain.mapper.UserMapper;

@Component
public class UserMapperImpl implements UserMapper {

  @Override
  public UserDto toDto(User user) {
    return UserDto.builder()
        .id(user.getId())
        .firstName(user.getFirstName())
        .lastName(user.getLastName())
        .email(user.getEmail())
        .phoneNumber(user.getPhoneNumber())
        .roles(user.getRoles())
        .build();
  }

}
