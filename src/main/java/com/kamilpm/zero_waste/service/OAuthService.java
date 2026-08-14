package com.kamilpm.zero_waste.service;

import java.util.List;
import java.util.UUID;

import com.kamilpm.zero_waste.domain.dto.Connections;
import com.kamilpm.zero_waste.domain.dto.OAuthFlow;
import com.kamilpm.zero_waste.domain.dto.OAuthSession;
import com.kamilpm.zero_waste.domain.dto.OAuthUserInfo;
import com.kamilpm.zero_waste.domain.entity.OAuthProvider;
import com.kamilpm.zero_waste.domain.entity.User;

public interface OAuthService {

  Connections getConnections();

  OAuthUserInfo authenticate(OAuthProvider provider, String code);

  String initiateLink(OAuthProvider provider);

  String initiateLogin(OAuthProvider provider);

  String create(OAuthFlow flow, UUID userId, OAuthProvider provider);

  OAuthSession consume(String state);

  OAuthUserInfo authenticateGithub(
      String code);

  OAuthUserInfo authenticateGoogle(
      String code);

  void linkOAuthAccount(
      UUID userId,
      OAuthUserInfo info);

  void unlinkAccount(OAuthProvider provider);

  void deleteAllByUserIds(List<UUID> ids);

  User processLogin(OAuthUserInfo info);

}
