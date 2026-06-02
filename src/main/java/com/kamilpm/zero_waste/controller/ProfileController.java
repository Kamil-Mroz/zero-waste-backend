package com.kamilpm.zero_waste.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kamilpm.zero_waste.domain.response.PublicUserProfileResponse;
import com.kamilpm.zero_waste.service.ProfileService;

import lombok.RequiredArgsConstructor;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@RestController
@RequestMapping(path = "/api/v{version}/profiles", version = "1")
@RequiredArgsConstructor
public class ProfileController {
  private final ProfileService profileService;

  @GetMapping("/{id}")
  public ResponseEntity<PublicUserProfileResponse> getProfile(@PathVariable UUID id) {
    return ResponseEntity.ok(profileService.getProfile(id));
  }

}
