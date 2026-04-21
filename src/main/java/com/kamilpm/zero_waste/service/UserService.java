package com.kamilpm.zero_waste.service;

import java.util.List;
import java.util.UUID;

import com.kamilpm.zero_waste.domain.entity.User;
import com.kamilpm.zero_waste.domain.request.BanRequest;
import com.kamilpm.zero_waste.domain.request.CreateUserRequest;
import com.kamilpm.zero_waste.domain.request.UnbanRequest;
import com.kamilpm.zero_waste.domain.request.UpdateUserRequest;

public interface UserService {

  List<User> getUsers();

  User getUser(UUID id);

  User createUser(CreateUserRequest userRequest);

  User updateUser(UUID id, UpdateUserRequest userRequest);

  void deleteUser(List<UUID> ids);

  void banUsers(BanRequest banRequest);

  void unbanUsers(UnbanRequest unbanRequest);
}
