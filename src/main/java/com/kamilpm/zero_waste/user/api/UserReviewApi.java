package com.kamilpm.zero_waste.user.api;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.kamilpm.zero_waste.common.dto.UserSummaryDto;
import com.kamilpm.zero_waste.user.mapper.UserMapper;
import com.kamilpm.zero_waste.user.repository.UserRepository;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserReviewApi {
  private final UserRepository userRepository;
  private final UserSummaryMapper userSummaryMapper;

  public UserSummaryDto getUserById(UUID userId) {
    return userRepository.findById(userId).map(userSummaryMapper::toDto)
        .orElseThrow(() -> new EntityNotFoundException("User not found"));

  }

  public Map<UUID, UserSummaryDto> getUsersById(Collection<UUID> ids) {
    return userRepository.findAllById(ids).stream()
        .collect(Collectors.toMap(user -> user.getId(), userSummaryMapper::toDto));
  }

}
