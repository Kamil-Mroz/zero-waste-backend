package com.kamilpm.zero_waste.image.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kamilpm.zero_waste.image.dto.ImageUploadRequest;
import com.kamilpm.zero_waste.image.dto.ImageUploadResponse;
import com.kamilpm.zero_waste.image.service.ImageService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PostMapping;

@RestController
@RequestMapping(path = "/api/v{version}/images", version = "1")
@RequiredArgsConstructor
public class ImageController {
  private final ImageService imageService;

  @PostMapping("/upload-urls")
  public ResponseEntity<ImageUploadResponse> createUploadUrls(@Valid @RequestBody ImageUploadRequest request) {

    return ResponseEntity.ok(imageService.createUploadUrls(request));
  }

}
