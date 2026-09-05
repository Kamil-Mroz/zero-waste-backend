package com.kamilpm.zero_waste.user.api;

import com.kamilpm.zero_waste.user.mapper.UserMapper;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.kamilpm.zero_waste.common.dto.UserSummaryDto;
import com.kamilpm.zero_waste.user.repository.UserRepository;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserItemApi {

  private final UserRepository userRepository;
  private final UserSummaryMapper userSummaryMapper;

  public boolean isUserDemo(UUID userId) {
    return userRepository.findById(userId).map(user -> user.getRole() == UserRole.DEMO).orElse(false);
  }

  public UserSummaryDto findByItemOwnerId(UUID userId) {

    return userRepository.findById(userId).map(userSummaryMapper::toDto)
        .orElseThrow(() -> new EntityNotFoundException("Owner not found"));
  }

  public Map<UUID, UserSummaryDto> getUsersByIds(Collection<UUID> ids) {
    return userRepository.findAllById(ids).stream()
        .collect(Collectors.toMap((user) -> user.getId(), userSummaryMapper::toDto));

  }

}
