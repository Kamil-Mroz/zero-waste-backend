package com.kamilpm.zero_waste.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import com.kamilpm.zero_waste.domain.entity.User;
import com.kamilpm.zero_waste.repository.UserRepository;
import com.kamilpm.zero_waste.service.UserService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

  private final UserRepository userRepository;

  @Override
  public List<User> getUsers() {
    return userRepository.findAll();
  }

}
