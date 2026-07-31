package com.kamilpm.zero_waste.domain.mapper;

import org.mapstruct.Mapper;

import com.kamilpm.zero_waste.domain.dto.UserSummaryDto;
import com.kamilpm.zero_waste.domain.entity.User;

@Mapper(componentModel = "spring")
public interface UserSummaryMapper {
  UserSummaryDto toDto(User user);

}
