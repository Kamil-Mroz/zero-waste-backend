package com.kamilpm.zero_waste.auth.service;

import java.security.SecureRandom;
import java.time.Duration;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import com.kamilpm.zero_waste.auth.api.AuthApi;
import com.kamilpm.zero_waste.auth.api.AuthenticatedUser;
import com.kamilpm.zero_waste.auth.dto.Connections;
import com.kamilpm.zero_waste.auth.dto.GithubEmail;
import com.kamilpm.zero_waste.auth.dto.GithubTokenResponse;
import com.kamilpm.zero_waste.auth.dto.GithubUserInfo;
import com.kamilpm.zero_waste.auth.dto.GoogleTokenResponse;
import com.kamilpm.zero_waste.auth.dto.GoogleUserInfo;
import com.kamilpm.zero_waste.auth.dto.OAuthFlow;
import com.kamilpm.zero_waste.auth.dto.OAuthSession;
import com.kamilpm.zero_waste.auth.dto.OAuthUserInfo;
import com.kamilpm.zero_waste.auth.entity.OAuthAccount;
import com.kamilpm.zero_waste.auth.entity.OAuthProvider;
import com.kamilpm.zero_waste.auth.properties.OAuthProperties;
import com.kamilpm.zero_waste.auth.repository.OAuthAccountRepository;
import com.kamilpm.zero_waste.common.exception.ConflictException;
import com.kamilpm.zero_waste.common.exception.EntityNotFoundException;
import com.kamilpm.zero_waste.common.exception.OAuthAccountRequiredException;
import com.kamilpm.zero_waste.common.exception.OAuthAuthenticationException;
import com.kamilpm.zero_waste.common.exception.UnauthorizedException;
import com.kamilpm.zero_waste.user.api.UsersDeletedEvent;
import com.kamilpm.zero_waste.user.api.UserApi;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import tools.jackson.databind.json.JsonMapper;

@Service
@RequiredArgsConstructor
public class OAuthService {

  private final OAuthAccountRepository oauthAccountRepository;
  private final StringRedisTemplate redisTemplate;
  private static final Duration LINK_EXPIRATION = Duration.ofMinutes(5);
  private final JsonMapper objectMapper;
  private final OAuthProperties properties;
  private final GithubOAuthClient githubLinkClient;
  private final GoogleOAuthClient googleLinkClient;
  private final AuthApi authApi;
  private final UserApi userApi;
  private final String OAUTH_KEY = "oauth:";

  public Connections getConnections() {
    AuthenticatedUser user = authApi.getRequiredAuthenticatedUser();
    return new Connections(getProviders(user.id()), user.password() != null);
  }

  public OAuthUserInfo authenticate(OAuthProvider provider, String code) {
    return switch (provider) {
      case GOOGLE -> authenticateGoogle(code);
      case GITHUB -> authenticateGithub(code);
    };
  }

  @Transactional
  public String initiateLogin(OAuthProvider provider) {
    String state = create(OAuthFlow.LOGIN, null, provider);
    return buildAuthorizationUrl(provider, state);
  }

  @Transactional
  public String initiateLink(OAuthProvider provider) {
    AuthenticatedUser user = authApi.getRequiredAuthenticatedUser();
    String state = create(OAuthFlow.LINK, user.id(), provider);
    return buildAuthorizationUrl(provider, state);
  }

  @Transactional
  public AuthenticatedUser processLogin(OAuthUserInfo info) {

    Optional<OAuthAccount> existingAccount = oauthAccountRepository.findByProviderAndProviderId(
        info.provider(),
        info.providerId());

    if (existingAccount.isPresent()) {
      AuthenticatedUser user = userApi.findById(existingAccount.get().getUserId());
      if (user.banActive()) {
        throw new UnauthorizedException("Account suspended");
      }

      return user;
    }

    Optional<AuthenticatedUser> existingUser = userApi.findAuthenticatedUserByEmail(info.email().toLowerCase());

    if (existingUser.isPresent()) {

      throw new OAuthAccountRequiredException(
          "An account with this email already exists. " + "Please log in and link your " + info.provider().name()
              + " account.");
    }

    AuthenticatedUser user = userApi.createOAuthUser(info.email(), info.nickname());

    OAuthAccount oauthAccount = OAuthAccount.builder()
        .userId(user.id())
        .provider(info.provider())
        .providerId(info.providerId())
        .build();
    oauthAccountRepository.save(oauthAccount);
    return user;
  }

  public String buildAuthorizationUrl(OAuthProvider provider, String state) {
    return switch (provider) {
      case GITHUB -> buildGithubAuthorizationUrl(state);
      case GOOGLE -> buildGoogleAuthorizationUrl(state);
      default -> throw new OAuthAuthenticationException("Unsupported oauth2 provider");
    };
  }

  public String create(OAuthFlow flow, UUID userId, OAuthProvider provider) {
    String state = generateState();
    OAuthSession session = new OAuthSession(userId, provider, state, flow);
    String key = OAUTH_KEY + state;
    try {
      String value = objectMapper.writeValueAsString(session);
      redisTemplate.opsForValue().set(key, value, LINK_EXPIRATION);
      return state;
    } catch (Exception e) {
      throw new IllegalStateException(e.getMessage());
    }
  }

  private String generateState() {
    byte[] bytes = new byte[32];
    new SecureRandom().nextBytes(bytes);
    return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
  }

  public OAuthSession consume(String state) {
    if (state == null || state.isBlank()) {
      throw new OAuthAuthenticationException("Missing Oauth state");
    }

    String key = OAUTH_KEY + state;
    String json = redisTemplate.opsForValue().get(key);

    if (json == null) {
      throw new OAuthAuthenticationException("Invalid or expired Oauth state");
    }

    redisTemplate.delete(key);

    try {
      return objectMapper.readValue(json, OAuthSession.class);
    } catch (Exception e) {
      throw new IllegalStateException("Invalid Oauth link session", e);
    }
  }

  private String buildGithubAuthorizationUrl(
      String state) {

    OAuthProperties.Github github = properties.github();

    return UriComponentsBuilder
        .fromUriString(
            github.authorizationUri())
        .queryParam(
            "client_id",
            github.clientId())
        .queryParam(
            "redirect_uri",
            github.redirectUri())
        .queryParam(
            "scope",
            "read:user user:email")
        .queryParam(
            "state",
            state)
        .queryParam("prompt", "select_account")
        .build()
        .encode()
        .toUriString();
  }

  private String buildGoogleAuthorizationUrl(
      String state) {

    OAuthProperties.Google google = properties.google();

    return UriComponentsBuilder
        .fromUriString(
            google.authorizationUri())
        .queryParam(
            "client_id",
            google.clientId())
        .queryParam(
            "redirect_uri",
            google.redirectUri())
        .queryParam(
            "response_type",
            "code")
        .queryParam(
            "scope",
            "openid profile email")
        .queryParam(
            "state",
            state)
        .queryParam(
            "prompt",
            "select_account")
        .build()
        .encode()
        .toUriString();
  }

  public OAuthUserInfo authenticateGithub(
      String code) {

    GithubTokenResponse token = githubLinkClient.exchangeCode(code);

    GithubUserInfo user = githubLinkClient.getUser(
        token.accessToken());

    List<GithubEmail> emails = githubLinkClient.getEmails(
        token.accessToken());

    GithubEmail primaryEmail = emails.stream()
        .filter(GithubEmail::primary)
        .findFirst()
        .orElseGet(() -> emails.stream()

            .filter(GithubEmail::verified)
            .findFirst()
            .orElse(null));

    if (primaryEmail == null) {
      throw new OAuthAuthenticationException(
          "GitHub did not provide a verified email");
    }

    return new OAuthUserInfo(
        OAuthProvider.GITHUB,
        String.valueOf(user.id()),
        primaryEmail.email(),
        user.login());
  }

  public OAuthUserInfo authenticateGoogle(
      String code) {

    GoogleTokenResponse token = googleLinkClient.exchangeCode(code);

    GoogleUserInfo user = googleLinkClient.getUserInfo(
        token.accessToken());

    if (user.sub() == null) {
      throw new OAuthAuthenticationException(
          "Google did not provide a user ID");
    }

    if (user.email() == null) {
      throw new OAuthAuthenticationException(
          "Google did not provide an email");
    }

    if (!user.emailVerified()) {
      throw new OAuthAuthenticationException(
          "Google email is not verified");
    }

    return new OAuthUserInfo(
        OAuthProvider.GOOGLE,
        user.sub(),
        user.email(),
        user.name() != null
            ? user.name()
            : user.email());
  }

  @Transactional
  public void linkOAuthAccount(
      UUID userId,
      OAuthUserInfo info) {

    AuthenticatedUser user = userApi.findById(userId);

    if (user.banActive()) {
      throw new UnauthorizedException(
          "Account suspended");
    }

    Optional<OAuthAccount> existing = oauthAccountRepository
        .findByProviderAndProviderId(
            info.provider(),
            info.providerId());

    if (existing.isPresent()) {

      if (existing.get()
          .getUserId()
          .equals(userId)) {

        throw new ConflictException(
            "This account is already linked");
      }

      throw new ConflictException(
          "This " + info.provider().toString().toLowerCase() + " account is already linked to another account");
    }

    boolean providerAlreadyLinked = oauthAccountRepository
        .existsByUserIdAndProvider(
            userId,
            info.provider());

    if (providerAlreadyLinked) {
      throw new ConflictException(
          "You already have a " +
              info.provider() +
              " account linked");
    }

    OAuthAccount account = OAuthAccount.builder()
        .userId(user.id())
        .provider(info.provider())
        .providerId(info.providerId())
        .build();

    oauthAccountRepository.save(account);
  }

  @Transactional
  public void unlinkAccount(OAuthProvider provider) {
    AuthenticatedUser user = authApi.getRequiredAuthenticatedUser();

    OAuthAccount account = oauthAccountRepository.findByUserIdAndProvider(user.id(), provider)
        .orElseThrow(() -> new EntityNotFoundException("Account not linked"));

    validateUserCanStillLogin(user);
    oauthAccountRepository.delete(account);

  }

  private void validateUserCanStillLogin(AuthenticatedUser user) {
    long linkedProviders = getProviders(user.id()).size();

    boolean hasPassword = user.password() != null && !user.password().isBlank();

    long remainingMethods = (hasPassword ? 1 : 0) + (linkedProviders - 1);
    if (remainingMethods <= 0) {
      throw new ConflictException("Cannot remove the last login method");
    }

  }

  private List<OAuthProvider> getProviders(UUID userId) {
    return oauthAccountRepository.findByUserId(userId).stream().map((account) -> account.getProvider()).toList();
  }

  @ApplicationModuleListener
  void on(UsersDeletedEvent event) {

    oauthAccountRepository.deleteByUserIdIn(event.ids());
  }
}
