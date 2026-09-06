package com.kamilpm.zero_waste.user.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kamilpm.zero_waste.user.dto.OwnProfileResponse;
import com.kamilpm.zero_waste.user.dto.PublicUserProfileResponse;
import com.kamilpm.zero_waste.user.service.ProfileService;

import lombok.RequiredArgsConstructor;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@RestController
@RequestMapping(path = "/api/v{version}/profiles", version = "1")
@RequiredArgsConstructor
public class ProfileController {
  private final ProfileService profileService;

  @GetMapping("/{id}")
  public ResponseEntity<PublicUserProfileResponse> getProfile(@PathVariable("id")  UUID id) {
    return ResponseEntity.ok(profileService.getProfile(id));
  }

  @GetMapping
  public ResponseEntity<OwnProfileResponse> getOwnProfile() {

    return ResponseEntity.ok(profileService.getOwnProfile());
  }
}
