package com.kamilpm.zero_waste.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kamilpm.zero_waste.domain.dto.UserDto;
import com.kamilpm.zero_waste.domain.entity.User;
import com.kamilpm.zero_waste.domain.mapper.UserMapper;
import com.kamilpm.zero_waste.domain.request.BanRequest;
import com.kamilpm.zero_waste.domain.request.CreateUserRequest;
import com.kamilpm.zero_waste.domain.request.UnbanRequest;
import com.kamilpm.zero_waste.domain.request.UpdateUserRequest;
import com.kamilpm.zero_waste.service.UserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.UUID;

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
  private final UserMapper userMapper;

  @GetMapping
  public ResponseEntity<List<UserDto>> getUsers() {
    List<User> users = userService.getUsers();
    List<UserDto> usersDto = users.stream().map(userMapper::toDto).toList();
    return ResponseEntity.ok(usersDto);
  }

  @GetMapping("/{id}")
  public ResponseEntity<UserDto> getUser(@PathVariable UUID id) {
    User user = userService.getUser(id);
    return ResponseEntity.ok(userMapper.toDto(user));
  }

  @PostMapping
  public ResponseEntity<UserDto> createUser(@Valid @RequestBody CreateUserRequest createUserRequest) {

    User user = userService.createUser(createUserRequest);

    return new ResponseEntity<>(userMapper.toDto(user), HttpStatus.CREATED);
  }

  @PutMapping("/{id}")
  public ResponseEntity<UserDto> updateUser(@PathVariable UUID id,
      @Valid @RequestBody UpdateUserRequest updateUserRequest) {
    User user = userService.updateUser(id, updateUserRequest);

    return ResponseEntity.ok(userMapper.toDto(user));
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
