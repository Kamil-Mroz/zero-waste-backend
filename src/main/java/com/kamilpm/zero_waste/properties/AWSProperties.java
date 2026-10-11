package com.kamilpm.zero_waste.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.aws")
public record AWSProperties(String endpoint, String publicEndpoint, String accessKeyId, String secretAccessKey,
    String region, String bucket,
    boolean pathStyleAccess) {
  public boolean hasStaticCredentials() {
    return accessKeyId != null && !accessKeyId.isBlank() && secretAccessKey != null && !secretAccessKey.isBlank();
  }

}
