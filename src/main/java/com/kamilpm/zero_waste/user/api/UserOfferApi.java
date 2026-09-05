package com.kamilpm.zero_waste.user.api;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.kamilpm.zero_waste.common.dto.UserSummaryWithEmailDto;
import com.kamilpm.zero_waste.notification.api.NotificationRecipient;
import com.kamilpm.zero_waste.user.repository.UserRepository;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserOfferApi {
  private final UserRepository userRepository;
  private final UserSummaryMapper userSummaryMapper;

  public String getUserEmail(UUID userId) {
    return userRepository.findById(userId).map(user -> user.getEmail())
        .orElseThrow(() -> new EntityNotFoundException("Buyer not found"));
  }

  public List<NotificationRecipient> getUsersEmail(List<UUID> userIds) {
    return userRepository.findAllById(userIds).stream()
        .map(user -> new NotificationRecipient(user.getId(), user.getEmail())).toList();
  }

  public boolean isUserDemo(UUID userId) {
    return userRepository.findById(userId).map(user -> user.getRole() == UserRole.DEMO).orElse(false);
  }

  public Map<UUID, UserSummaryWithEmailDto> getUsersByIds(Collection<UUID> ids) {
    return userRepository.findAllById(ids).stream()
        .collect(Collectors.toMap((user) -> user.getId(), userSummaryMapper::toWithEmailDto));
  }

}
