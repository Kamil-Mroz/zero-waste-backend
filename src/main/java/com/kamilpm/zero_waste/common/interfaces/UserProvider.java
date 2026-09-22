package com.kamilpm.zero_waste.common.interfaces;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import com.kamilpm.zero_waste.common.dto.NotificationRecipient;
import com.kamilpm.zero_waste.common.dto.UserAuthenticationData;
import com.kamilpm.zero_waste.common.dto.UserSummaryDto;
import com.kamilpm.zero_waste.common.dto.UserSummaryWithEmailDto;

public interface UserProvider {

  public UserAuthenticationData findAuthenticationData(String email);

  public void savePassword(UUID userId, String passwordHash);

  public UserAuthenticationData getDemoUser();

  public UserAuthenticationData findById(UUID id);

  public UserSummaryDto findUserSummaryById(UUID id);

  public Optional<UserAuthenticationData> findAuthenticatedUserByEmail(String email);

  public UserAuthenticationData createOAuthUser(String email, String nickname);

  public boolean isUserDemo(UUID userId);

  public Map<UUID, UserAuthenticationData> getUsersByIds(Collection<UUID> ids);

  public Map<UUID, UserSummaryDto> getUserSummaryByIds(Collection<UUID> ids);

  public String getUserEmail(UUID userId);

  public Map<UUID, UserSummaryWithEmailDto> getUserSummaryWithEmailByIds(Collection<UUID> ids);

  public List<NotificationRecipient> getUsersEmail(List<UUID> userIds);

  public void userExists(UUID subjectId, UUID userId);

  public void banUser(UUID adminId, UUID reportId, String adminNote);

}
