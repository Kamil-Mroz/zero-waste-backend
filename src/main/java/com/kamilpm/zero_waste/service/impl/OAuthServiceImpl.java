package com.kamilpm.zero_waste.service.impl;

import java.security.SecureRandom;
import java.time.Duration;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import com.kamilpm.zero_waste.config.OAuthProperties;
import com.kamilpm.zero_waste.domain.dto.Connections;
import com.kamilpm.zero_waste.domain.dto.GithubEmail;
import com.kamilpm.zero_waste.domain.dto.GithubTokenResponse;
import com.kamilpm.zero_waste.domain.dto.GithubUserInfo;
import com.kamilpm.zero_waste.domain.dto.GoogleTokenResponse;
import com.kamilpm.zero_waste.domain.dto.GoogleUserInfo;
import com.kamilpm.zero_waste.domain.dto.OAuthFlow;
import com.kamilpm.zero_waste.domain.dto.OAuthSession;
import com.kamilpm.zero_waste.domain.dto.OAuthUserInfo;
import com.kamilpm.zero_waste.domain.entity.OAuthAccount;
import com.kamilpm.zero_waste.domain.entity.OAuthProvider;
import com.kamilpm.zero_waste.domain.entity.User;
import com.kamilpm.zero_waste.domain.entity.UserRole;
import com.kamilpm.zero_waste.exception.ConflictException;
import com.kamilpm.zero_waste.exception.EntityNotFoundException;
import com.kamilpm.zero_waste.exception.OAuthAccountRequiredException;
import com.kamilpm.zero_waste.exception.OAuthAuthenticationException;
import com.kamilpm.zero_waste.exception.UnauthorizedException;
import com.kamilpm.zero_waste.repository.OAuthAccountRepository;
import com.kamilpm.zero_waste.repository.UserRepository;
import com.kamilpm.zero_waste.service.AuthService;
import com.kamilpm.zero_waste.service.GithubOAuthClient;
import com.kamilpm.zero_waste.service.GoogleOAuthClient;
import com.kamilpm.zero_waste.service.OAuthService;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import tools.jackson.databind.json.JsonMapper;

@Service
@RequiredArgsConstructor
public class OAuthServiceImpl implements OAuthService {

  private final UserRepository userRepository;
  private final OAuthAccountRepository oauthAccountRepository;
  private final StringRedisTemplate redisTemplate;
  private static final Duration LINK_EXPIRATION = Duration.ofMinutes(5);
  private final JsonMapper objectMapper;
  private final OAuthProperties properties;
  private final GithubOAuthClient githubLinkClient;
  private final GoogleOAuthClient googleLinkClient;
  private final AuthService authService;
  private final String OAUTH_KEY = "oauth:";

  @Override
  public Connections getConnections() {
    User user = authService.getRequiredAuthenticatedUser();
    return new Connections(getProviders(user.getId()), user.getPassword() != null);
  }

  @Override
  public OAuthUserInfo authenticate(OAuthProvider provider, String code) {
    return switch (provider) {
      case GOOGLE -> authenticateGoogle(code);
      case GITHUB -> authenticateGithub(code);
    };
  }

  @Override
  @Transactional
  public String initiateLogin(OAuthProvider provider) {
    String state = create(OAuthFlow.LOGIN, null, provider);
    return buildAuthorizationUrl(provider, state);
  }

  @Override
  @Transactional
  public String initiateLink(OAuthProvider provider) {
    User user = authService.getRequiredAuthenticatedUser();
    String state = create(OAuthFlow.LINK, user.getId(), provider);
    return buildAuthorizationUrl(provider, state);
  }

  @Transactional
  public User processLogin(OAuthUserInfo info) {

    Optional<OAuthAccount> existingAccount = oauthAccountRepository.findByProviderAndProviderId(
        info.provider(),
        info.providerId());

    if (existingAccount.isPresent()) {
      User user = existingAccount.get().getUser();
      if (user.isBanActive()) {
        throw new UnauthorizedException("Account suspended");
      }

      return user;
    }

    Optional<User> existingUser = userRepository.findByEmail(info.email().toLowerCase());

    if (existingUser.isPresent()) {

      throw new OAuthAccountRequiredException(
          "An account with this email already exists. " + "Please log in and link your " + info.provider().name()
              + " account.");
    }

    User user = User.builder()
        .nickname(info.nickname())
        .email(info.email().toLowerCase())
        .password(null)
        .role(UserRole.USER)
        .banActive(false)
        .bannedUntil(null)
        .build();
    user = userRepository.save(user);
    OAuthAccount oauthAccount = OAuthAccount.builder()
        .user(user)
        .provider(info.provider())
        .providerId(info.providerId())
        .build();
    oauthAccountRepository.save(oauthAccount);
    return user;
  }

  @Override
  public void deleteAllByUserIds(List<UUID> ids) {
    oauthAccountRepository.deleteByUser_IdIn(ids);
  }

  public String buildAuthorizationUrl(OAuthProvider provider, String state) {
    return switch (provider) {
      case GITHUB -> buildGithubAuthorizationUrl(state);
      case GOOGLE -> buildGoogleAuthorizationUrl(state);
      default -> throw new OAuthAuthenticationException("Unsupported oauth2 provider");
    };
  }

  @Override
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

  @Override
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

  @Override
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

  @Override
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

  @Override
  @Transactional
  public void linkOAuthAccount(
      UUID userId,
      OAuthUserInfo info) {

    User user = userRepository.findById(userId)
        .orElseThrow(() -> new EntityNotFoundException(
            "User not found"));

    if (user.isBanActive()) {
      throw new UnauthorizedException(
          "Account suspended");
    }

    Optional<OAuthAccount> existing = oauthAccountRepository
        .findByProviderAndProviderId(
            info.provider(),
            info.providerId());

    if (existing.isPresent()) {

      if (existing.get()
          .getUser()
          .getId()
          .equals(userId)) {

        throw new ConflictException(
            "This account is already linked");
      }

      throw new ConflictException(
          "This " + info.provider().toString().toLowerCase() + " account is already linked to another account");
    }

    boolean providerAlreadyLinked = oauthAccountRepository
        .existsByUser_IdAndProvider(
            userId,
            info.provider());

    if (providerAlreadyLinked) {
      throw new ConflictException(
          "You already have a " +
              info.provider() +
              " account linked");
    }

    OAuthAccount account = OAuthAccount.builder()
        .user(user)
        .provider(info.provider())
        .providerId(info.providerId())
        .build();

    oauthAccountRepository.save(account);
  }

  @Transactional
  @Override
  public void unlinkAccount(OAuthProvider provider) {
    User user = authService.getRequiredAuthenticatedUser();

    OAuthAccount account = oauthAccountRepository.findByUser_IdAndProvider(user.getId(), provider)
        .orElseThrow(() -> new EntityNotFoundException("Account not linked"));

    validateUserCanStillLogin(user);
    oauthAccountRepository.delete(account);

  }

  private void validateUserCanStillLogin(User user) {
    long linkedProviders = getProviders(user.getId()).size();

    boolean hasPassword = user.getPassword() != null && !user.getPassword().isBlank();

    long remainingMethods = (hasPassword ? 1 : 0) + (linkedProviders - 1);
    if (remainingMethods <= 0) {
      throw new ConflictException("Cannot remove the last login method");
    }

  }

  private List<OAuthProvider> getProviders(UUID userId) {
    return oauthAccountRepository.findByUser_Id(userId).stream().map((account) -> account.getProvider()).toList();
  }
}
