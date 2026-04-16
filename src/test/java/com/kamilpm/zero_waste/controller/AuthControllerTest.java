package com.kamilpm.zero_waste.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.ProblemDetail;
import org.springframework.test.web.servlet.client.RestTestClient;

import com.kamilpm.zero_waste.domain.dto.UserDto;
import com.kamilpm.zero_waste.domain.request.LoginRequest;
import com.kamilpm.zero_waste.domain.request.RegisterRequest;
import com.kamilpm.zero_waste.domain.response.AuthResponse;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class AuthControllerTest {
  private static final String PASSWORD = "SecurePassword123!";
  private static final String COOKIE = "refreshToken";

  @LocalServerPort
  private int port;

  RestTestClient client;

  @BeforeEach
  void setUp() {
    client = RestTestClient.bindToServer()
        .baseUrl("http://localhost:" + port + "/api/v1/auth")
        .build();
  }

  private RegisterRequest defaultRegisterRequest(String email) {
    return RegisterRequest.builder()
        .firstName("John")
        .lastName("Doe")
        .phoneNumber("+39 342 341 235")
        .email(email)
        .password(PASSWORD)
        .build();
  }

  private void register(String email) {
    client.post().uri("/register")
        .body(defaultRegisterRequest(email))
        .exchange()
        .expectStatus()
        .isCreated();
  }

  private String loginAndGetRefreshToken(String email) {
    return client.post()
        .uri("/login")
        .body(LoginRequest.builder()
            .email(email)
            .password(PASSWORD)
            .build())
        .exchange()
        .expectStatus().isOk()
        .returnResult()
        .getResponseCookies()
        .getFirst(COOKIE)
        .getValue();
  }

  // private String loginAndGetAccessToken(String email) {
  // return client.post()
  // .uri("/login")
  // .body(LoginRequest.builder()
  // .email(email)
  // .password(PASSWORD)
  // .build())
  // .exchange()
  // .expectStatus().isOk().expectBody(AuthResponse.class)
  // .returnResult().getResponseBody().getAccessToken();
  // }

  @Test
  void testRegisterShouldPass() {
    String email = "john.doe@example.com";

    RegisterRequest registerRequest = defaultRegisterRequest(email);

    UserDto user = client.post()
        .uri("/register")
        .body(registerRequest)
        .exchange()
        .expectStatus()
        .isCreated()
        .expectBody(UserDto.class)
        .returnResult()
        .getResponseBody();

    assertEquals(email, user.getEmail());
    assertEquals(registerRequest.getFirstName(), user.getFirstName());
  }

  @Test
  void testRegisterShouldFail() {
    String email = "invalidEmail";

    RegisterRequest registerRequest = defaultRegisterRequest(email);

    ProblemDetail problem = client.post()
        .uri("/register")
        .body(registerRequest)
        .exchange()
        .expectStatus()
        .isBadRequest()
        .expectBody(ProblemDetail.class)
        .returnResult()
        .getResponseBody();

    var properties = problem.getProperties();
    assertNotNull(properties);
    Map<String, Object> errors = (Map<String, Object>) properties.get("errors");
    assertNotNull(errors);

    assertEquals(1, errors.size());
    assertTrue(errors.containsKey("email"));

    Map<String, Object> emailError = (Map<String, Object>) errors.get("email");

    assertEquals("Must be a valid email", emailError.get("message"));
  }

  @Test
  void testLogout() {
    client.post().uri("/logout")
        .exchange()
        .expectStatus()
        .isNoContent()
        .expectBody()
        .isEmpty();
  }

  @Test
  void testLoginShouldPass() {
    String email = "john.doe2@example.com";

    register(email);

    AuthResponse response = client.post()
        .uri("/login")
        .body(LoginRequest.builder()
            .email(email)
            .password(PASSWORD)
            .build())
        .exchange()
        .expectStatus()
        .isOk()
        .expectCookie().exists(COOKIE)
        .expectBody(AuthResponse.class)
        .returnResult()
        .getResponseBody();

    assertNotNull(response.getAccessToken());
    assertFalse(response.getAccessToken().isBlank());
  }

  @Test
  void testLoginShouldFail() {
    String email = "noExisting@example.com";

    ProblemDetail problem = client.post()
        .uri("/login")
        .body(LoginRequest.builder()
            .email(email)
            .password(PASSWORD)
            .build())
        .exchange()
        .expectStatus()
        .isUnauthorized()
        .expectBody(ProblemDetail.class)
        .returnResult()
        .getResponseBody();

    String details = problem.getDetail();
    assertFalse(details.isBlank());
    assertEquals("Invalid credentials", details);
  }

  @Test
  void testRefreshShouldPass() {
    String email = "john.doe3@example.com";

    register(email);

    String cookie = loginAndGetRefreshToken(email);

    AuthResponse response = client.post()
        .uri("/refresh")
        .cookie(COOKIE, cookie)
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(AuthResponse.class)
        .returnResult()
        .getResponseBody();

    assertNotNull(response.getAccessToken());
    assertFalse(response.getAccessToken().isBlank());
    assertNotNull(response.getUser());
    assertEquals(email, response.getUser().getEmail());
  }

  @Test
  void testRefreshShouldFail() {

    ProblemDetail problem = client.post()
        .uri("/refresh")
        .exchange()
        .expectStatus()
        .isUnauthorized()
        .expectBody(ProblemDetail.class)
        .returnResult()
        .getResponseBody();

    String details = problem.getDetail();
    assertFalse(details.isBlank());
    assertEquals("Refresh token cookie not found", details);
  }

  // @Test
  // void testGetCurrentUserShouldPass() {

  // String email = "john.doe5@example.com";
  // register(email);
  // String accessToken = loginAndGetAccessToken(email);
  // CurrentUserDto user = client.get().uri("/me").header("Authorization", "Bearer
  // " + accessToken).exchange()
  // .expectStatus()
  // .isOk().expectBody(CurrentUserDto.class).returnResult().getResponseBody();
  // assertEquals(email, user.getEmail());
  // }

  // @Test
  // void testGetCurrentUserShouldFail() {

  // ErrorResponse error = client.get().uri("/me").header("Authorization", "Bearer
  // Invalid").exchange().expectStatus()
  // .isUnauthorized().expectBody(ErrorResponse.class).returnResult().getResponseBody();
  // assertEquals("Token invalid or expired", error.getDetail());
  // }
}
