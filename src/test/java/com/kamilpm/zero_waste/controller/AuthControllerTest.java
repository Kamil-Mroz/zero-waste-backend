package com.kamilpm.zero_waste.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.web.servlet.client.RestTestClient;

import com.kamilpm.zero_waste.domain.entity.User;
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
        .location("Texas")
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

  private String loginAndGetRefreshToken(String email, String password) {
    return client.post()
        .uri("/login")
        .body(LoginRequest.builder()
            .email(email)
            .password(password)
            .build())
        .exchange()
        .expectStatus().isOk()
        .returnResult()
        .getResponseCookies()
        .getFirst(COOKIE)
        .getValue();
  }

  @Test
  void testRegister() {
    String email = "john.doe@example.com";

    RegisterRequest registerRequest = defaultRegisterRequest(email);

    User user = client.post()
        .uri("/register")
        .body(registerRequest)
        .exchange()
        .expectStatus()
        .isCreated()
        .expectBody(User.class)
        .returnResult()
        .getResponseBody();

    assertEquals(email, user.getEmail());
    assertEquals(registerRequest.getFirstName(), user.getFirstName());
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
  void testLogin() {
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
  void testRefresh() {
    String email = "john.doe3@example.com";

    register(email);

    String cookie = loginAndGetRefreshToken(email, PASSWORD);

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
  }

}
