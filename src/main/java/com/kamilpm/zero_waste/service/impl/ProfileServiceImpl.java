package com.kamilpm.zero_waste.service.impl;

import java.time.Instant;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.kamilpm.zero_waste.domain.entity.User;
import com.kamilpm.zero_waste.domain.response.PublicUserProfileResponse;
import com.kamilpm.zero_waste.service.ItemService;
import com.kamilpm.zero_waste.service.ProfileService;
import com.kamilpm.zero_waste.service.UserService;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Transactional
@Service
@RequiredArgsConstructor
public class ProfileServiceImpl implements ProfileService {

  private final UserService userService;
  private final ItemService itemService;

  @Override
  public PublicUserProfileResponse getProfile(UUID userId) {

    // UUID id,
    // String firstName,
    // String lastName,
    // Instant joinedAt,
    // int itemCount
    User user = userService.getUser(userId);
    int itemCount = itemService.getUserItemCount(userId);
    return new PublicUserProfileResponse(userId, user.getFirstName(), user.getLastName(), user.getJoinedAt(),
        itemCount);
  }
}
