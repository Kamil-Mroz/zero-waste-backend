package com.kamilpm.zero_waste.image.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kamilpm.zero_waste.common.exception.EntityNotFoundException;
import com.kamilpm.zero_waste.image.entity.Image;
import com.kamilpm.zero_waste.image.service.ImageService;

import lombok.RequiredArgsConstructor;

import java.util.UUID;

import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@RestController
@RequestMapping(path = "/api/v{version}/images", version = "1")
@RequiredArgsConstructor
public class ImageController {
  private final ImageService imageService;


  @GetMapping("/{id}")
  public ResponseEntity<Resource> getResource(@PathVariable("id") UUID id) {
    Image image = imageService.getImageById(id);

    Resource resource = imageService.getImageAsResource(image.getStoredName())
        .orElseThrow(() -> new EntityNotFoundException("Image not found"));

    return ResponseEntity.ok().contentType(MediaType.parseMediaType(image.getMimeType()))
        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + image.getOriginalName() + "\"")
        .body(resource);
  }

}
