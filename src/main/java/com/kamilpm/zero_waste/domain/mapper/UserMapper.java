package com.kamilpm.zero_waste.domain.mapper;

import com.kamilpm.zero_waste.domain.dto.UserDto;
import com.kamilpm.zero_waste.domain.entity.User;

public interface UserMapper {

  UserDto toDto(User user);

}
