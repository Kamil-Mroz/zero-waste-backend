package com.kamilpm.zero_waste.user.api;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import com.kamilpm.zero_waste.auth.api.RevokeRefreshTokenEvent;
import com.kamilpm.zero_waste.common.dto.UserSummaryDto;
import com.kamilpm.zero_waste.common.exception.ConflictException;
import com.kamilpm.zero_waste.common.exception.EntityNotFoundException;
import com.kamilpm.zero_waste.common.exception.ForbiddenException;
import com.kamilpm.zero_waste.notification.api.SendBanNotificationEvent;
import com.kamilpm.zero_waste.user.entity.User;
import com.kamilpm.zero_waste.user.entity.UserBan;
import com.kamilpm.zero_waste.user.repository.UserBanRepository;
import com.kamilpm.zero_waste.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserReportApi {
  private final UserRepository userRepository;
  private final UserSummaryMapper userSummaryMapper;
  private final UserBanRepository userBanRepository;
  private final ApplicationEventPublisher events;

  public void userExists(UUID subjectId, UUID userId) {
    User reportedUser = userRepository.findById(subjectId)
        .orElseThrow(() -> new EntityNotFoundException("User not found"));

    if (Objects.equals(reportedUser.getId(), userId))
      throw new ForbiddenException("You can not report yourself");

    if (Objects.equals(reportedUser.getRole(), UserRole.DEMO))
      throw new ForbiddenException("Unable to interact with demo users");

    if (reportedUser.isBanActive())
      throw new ForbiddenException("Unable to report banned user");

  }

  public Map<UUID, UserSummaryDto> getUsersByIds(Collection<UUID> ids) {
    return userRepository.findAllById(ids).stream()
        .collect(Collectors.toMap((user) -> user.getId(), userSummaryMapper::toDto));
  }

  public void banUser(UUID adminId, UUID reportId, String adminNote) {

    User userToBan = userRepository.findById(reportId)
        .orElseThrow(() -> new EntityNotFoundException("User not found"));

    if (userToBan.isBanActive() && userToBan.getBannedUntil() == null) {
      throw new ConflictException("User already banned");
    }

    userToBan.setBanActive(true);
    userToBan.setBannedUntil(null);

    UserBan userBan = UserBan.builder()
        .userId(userToBan.getId())
        .reason(adminNote)
        .createdAt(Instant.now())
        .bannedBy(adminId)
        .expiresAt(null)
        .build();
    userRepository.save(userToBan);
    userBanRepository.save(userBan);

    events.publishEvent(new RevokeRefreshTokenEvent(List.of(userToBan.getId())));
    events.publishEvent(new SendBanNotificationEvent(userToBan.getEmail()));

  }

}
