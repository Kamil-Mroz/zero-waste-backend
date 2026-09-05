package com.kamilpm.zero_waste.user.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.kamilpm.zero_waste.common.annotation.RateLimit;
import com.kamilpm.zero_waste.common.dto.PageResponse;
import com.kamilpm.zero_waste.user.api.UserDto;
import com.kamilpm.zero_waste.user.api.UserRole;
import com.kamilpm.zero_waste.user.dto.BanRequest;
import com.kamilpm.zero_waste.user.dto.CreateUserRequest;
import com.kamilpm.zero_waste.user.dto.UnbanRequest;
import com.kamilpm.zero_waste.user.dto.UpdateUserRequest;
import com.kamilpm.zero_waste.user.service.UserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequestMapping(path = "/api/v{version}/users", version = "1")
@RequiredArgsConstructor
public class UserController {

  private final UserService userService;

  @GetMapping
  public ResponseEntity<PageResponse<UserDto>> getUsers(
      @RequestParam(value = "page", defaultValue = "0") int page,
      @RequestParam(value = "size", defaultValue = "20") int size,
      @RequestParam(value = "text", required = false) String text,
      @RequestParam(value = "roles", required = false) List<UserRole> roles) {
    Page<UserDto> users = userService.getUsersWithoutCurrentUser(text, roles, PageRequest.of(page, size));

    return ResponseEntity.ok(new PageResponse<>(users.getContent(),
        users.getNumber(), users.getSize(),
        users.getTotalElements(), users.getTotalPages()));
  }

  @GetMapping("/{id}")
  public ResponseEntity<UserDto> getUser(@PathVariable UUID id) {
    UserDto user = userService.getUser(id);
    return ResponseEntity.ok(user);
  }

  @RateLimit(action = "create-user", limit = 10, window = 10, unit = ChronoUnit.MINUTES)
  @PostMapping
  public ResponseEntity<UserDto> createUser(@Valid @RequestBody CreateUserRequest createUserRequest) {

    UserDto user = userService.createUser(createUserRequest);

    return new ResponseEntity<>(user, HttpStatus.CREATED);
  }

  @PutMapping("/{id}")
  @RateLimit(action = "update-user", limit = 20, window = 1, unit = ChronoUnit.MINUTES)
  public ResponseEntity<UserDto> updateUser(@PathVariable UUID id,
      @Valid @RequestBody UpdateUserRequest updateUserRequest) {
    UserDto user = userService.updateUser(id, updateUserRequest);

    return ResponseEntity.ok(user);
  }

  @RateLimit(action = "ban-user", limit = 10, window = 10, unit = ChronoUnit.MINUTES)
  @PostMapping("/ban")
  public ResponseEntity<Void> banUser(@Valid @RequestBody BanRequest banRequest) {
    userService.banUsers(banRequest);

    return ResponseEntity.noContent().build();
  }

  @RateLimit(action = "unban-user", limit = 10, window = 10, unit = ChronoUnit.MINUTES)
  @PostMapping("/unban")
  public ResponseEntity<Void> unbanUser(@Valid @RequestBody UnbanRequest unbanRequest) {
    userService.unbanUsers(unbanRequest);

    return ResponseEntity.noContent().build();
  }

  @RateLimit(action = "delete-users", limit = 5, window = 10, unit = ChronoUnit.MINUTES)
  @DeleteMapping
  public ResponseEntity<Void> deleteUsers(@RequestBody List<UUID> ids) {
    userService.deleteUser(ids);

    return ResponseEntity.noContent().build();
  }

  @RateLimit(action = "delete-own-account", limit = 3, window = 1, unit = ChronoUnit.HOURS)
  @DeleteMapping("/account")
  public ResponseEntity<Void> deleteOwnAccount() {
    userService.deleteOwnAccount();

    return ResponseEntity.noContent().build();
  }

}
