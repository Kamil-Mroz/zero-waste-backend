package com.kamilpm.zero_waste.image.dto;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record ImageUploadRequest(@NotEmpty @Size(max = 5) List<@Valid FileMetadata> files) {
  public record FileMetadata(@NotEmpty @Size(max = 255) String filename,
      @NotEmpty String contentType,
      @Positive long size) {
  }

}
