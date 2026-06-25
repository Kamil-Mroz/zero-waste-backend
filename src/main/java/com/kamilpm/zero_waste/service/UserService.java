package com.kamilpm.zero_waste.service;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.kamilpm.zero_waste.domain.dto.UserDto;
import com.kamilpm.zero_waste.domain.entity.UserRole;
import com.kamilpm.zero_waste.domain.request.BanRequest;
import com.kamilpm.zero_waste.domain.request.CreateUserRequest;
import com.kamilpm.zero_waste.domain.request.UnbanRequest;
import com.kamilpm.zero_waste.domain.request.UpdateUserRequest;

public interface UserService {

  Page<UserDto> getUsersWithoutCurrentUser(String text, List<UserRole> roles, Pageable pageable);

  UserDto getUser(UUID id);

  UserDto createUser(CreateUserRequest userRequest);

  UserDto updateUser(UUID id, UpdateUserRequest userRequest);

  void deleteUser(List<UUID> ids);

  void banUsers(BanRequest banRequest);

  void unbanUsers(UnbanRequest unbanRequest);
}
