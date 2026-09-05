package com.kamilpm.zero_waste.user.api;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.kamilpm.zero_waste.auth.api.AuthenticatedUser;
import com.kamilpm.zero_waste.user.mapper.UserMapper;
import com.kamilpm.zero_waste.user.repository.UserRepository;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserBlogApi {
  private final UserRepository userRepository;
  private final UserMapper userMapper;

  public Set<UUID> findExcludedAuthorIdsForPublicContent() {
    return userRepository.findIdsByBanActiveTrueAndRoleWriterOrRoleDemo();
  }

  public boolean isUserDemo(UUID userId) {
    return userRepository.findById(userId).map(user -> user.getRole() == UserRole.DEMO).orElse(false);
  }

  public Map<UUID, AuthenticatedUser> getAuthorsByIds(List<UUID> ids) {
    return userRepository.findAllById(ids).stream()
        .collect(Collectors.toMap((user) -> user.getId(), userMapper::toAuthenticatedUser));
  }

  public AuthenticatedUser getAuthorById(UUID id) {
    return userRepository.findById(id).map(user -> userMapper.toAuthenticatedUser(user))
        .orElseThrow(() -> new EntityNotFoundException("Author not found"));
  }
}
