package com.kamilpm.zero_waste.image.dto;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public record ImageUploadResponse(List<UploadInstruction> uploads) {
  public record UploadInstruction(UUID uploadId, String url, String method, Map<String, String> headers) {
  }

}
