package com.kamilpm.zero_waste.domain.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BlogRequest {

  @NotBlank
  @Size(max = 255, message = "Title can not exceed 255 characters")
  private String title;
  @NotBlank
  @Size(max = 255, message = "Description can not exceed 255 characters")
  private String description;
  @NotBlank
  private String content;
}
