package com.kamilpm.zero_waste.config;

import java.net.URI;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.kamilpm.zero_waste.properties.AWSProperties;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

@Configuration
@EnableConfigurationProperties(AWSProperties.class)
public class AWSConfig {
  @Bean
  S3Client s3Client(AWSProperties properties) {
    var builder = S3Client.builder().region(software.amazon.awssdk.regions.Region.of(properties.region()))
        .forcePathStyle(properties.pathStyleAccess());

    if (properties.endpoint() != null && !properties.endpoint().isBlank()) {
      builder.endpointOverride(URI.create(properties.endpoint()));
    }

    if (properties.hasStaticCredentials()) {
      builder.credentialsProvider(StaticCredentialsProvider
          .create(AwsBasicCredentials.create(properties.accessKeyId(), properties.secretAccessKey())));
    } else {
      builder.credentialsProvider(DefaultCredentialsProvider.builder().build());
    }
    return builder.build();

  }

  @Bean
  S3Presigner s3Presigner(AWSProperties properties) {
    var builder = S3Presigner.builder().region(Region.of(properties.region()));

    if (properties.publicEndpoint() != null
        && !properties.publicEndpoint().isBlank()) {
      builder.endpointOverride(
          URI.create(properties.publicEndpoint()));
    }

    if (properties.hasStaticCredentials()) {
      builder.credentialsProvider(
          StaticCredentialsProvider.create(
              AwsBasicCredentials.create(
                  properties.accessKeyId(),
                  properties.secretAccessKey())));
    } else {
      builder.credentialsProvider(
          DefaultCredentialsProvider.builder().build());
    }

    builder.serviceConfiguration(
        S3Configuration.builder()
            .pathStyleAccessEnabled(
                properties.pathStyleAccess())
            .build());

    return builder.build();
  }

}
