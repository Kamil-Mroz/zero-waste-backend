package com.kamilpm.zero_waste.domain.mapper;

import org.mapstruct.Mapper;

import com.kamilpm.zero_waste.domain.dto.NotificationDto;
import com.kamilpm.zero_waste.domain.entity.Notification;

@Mapper(componentModel = "spring")
public interface NotificationMapper {
  NotificationDto toDto(Notification notification);

}
