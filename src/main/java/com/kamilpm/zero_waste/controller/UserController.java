package com.kamilpm.zero_waste.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kamilpm.zero_waste.domain.entity.User;
import com.kamilpm.zero_waste.service.AuthService;
import com.kamilpm.zero_waste.service.UserService;

import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;

@RestController
@RequestMapping(path = "/api/v{version}/users", version = "1")
// @RequestMapping(path = "/api/v1/users")
@RequiredArgsConstructor
public class UserController {

  private final UserService userService;
  private final AuthService authService;

  @GetMapping
  public List<User> getUsers() {

    authService.getAuthenticatedUser();
    return userService.getUsers();
  }

}
