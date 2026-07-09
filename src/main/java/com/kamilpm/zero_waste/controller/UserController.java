package com.kamilpm.zero_waste.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.kamilpm.zero_waste.domain.dto.UserDto;
import com.kamilpm.zero_waste.domain.entity.UserRole;
import com.kamilpm.zero_waste.domain.request.BanRequest;
import com.kamilpm.zero_waste.domain.request.CreateUserRequest;
import com.kamilpm.zero_waste.domain.request.UnbanRequest;
import com.kamilpm.zero_waste.domain.request.UpdateUserRequest;
import com.kamilpm.zero_waste.domain.response.PageResponse;
import com.kamilpm.zero_waste.service.UserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

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

    return ResponseEntity.ok(new PageResponse<>(users.getContent(), users.getNumber(), users.getSize(),
        users.getTotalElements(), users.getTotalPages()));
  }

  @GetMapping("/{id}")
  public ResponseEntity<UserDto> getUser(@PathVariable UUID id) {
    UserDto user = userService.getUser(id);
    return ResponseEntity.ok(user);
  }

  @PostMapping
  public ResponseEntity<UserDto> createUser(@Valid @RequestBody CreateUserRequest createUserRequest) {

    UserDto user = userService.createUser(createUserRequest);

    return new ResponseEntity<>(user, HttpStatus.CREATED);
  }

  @PutMapping("/{id}")
  public ResponseEntity<UserDto> updateUser(@PathVariable UUID id,
      @Valid @RequestBody UpdateUserRequest updateUserRequest) {
    UserDto user = userService.updateUser(id, updateUserRequest);

    return ResponseEntity.ok(user);
  }

  @PostMapping("/ban")
  public ResponseEntity<Void> banUser(@Valid @RequestBody BanRequest banRequest) {
    userService.banUsers(banRequest);

    return ResponseEntity.noContent().build();
  }

  @PostMapping("/unban")
  public ResponseEntity<Void> unbanUser(@Valid @RequestBody UnbanRequest unbanRequest) {
    userService.unbanUsers(unbanRequest);

    return ResponseEntity.noContent().build();
  }

  @DeleteMapping
  public ResponseEntity<Void> deleteUsers(@RequestBody List<UUID> ids) {
    userService.deleteUser(ids);

    return ResponseEntity.noContent().build();
  }

}
