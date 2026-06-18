package com.kamilpm.zero_waste.domain.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.kamilpm.zero_waste.domain.dto.UserDto;
import com.kamilpm.zero_waste.domain.entity.User;

@Mapper(componentModel = "spring")
public interface UserMapper {

  @Mapping(target = "hasActiveBan", source = "banActive")
  UserDto toDto(User user);

}
