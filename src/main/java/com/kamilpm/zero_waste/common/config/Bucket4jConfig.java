package com.kamilpm.zero_waste.common.config;

import java.time.Duration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;

import io.github.bucket4j.distributed.ExpirationAfterWriteStrategy;
import io.github.bucket4j.redis.lettuce.Bucket4jLettuce;
import io.github.bucket4j.redis.lettuce.cas.LettuceBasedProxyManager;
import io.lettuce.core.RedisClient;

@Configuration
public class Bucket4jConfig {
  @Bean
  public LettuceBasedProxyManager<byte[]> proxyManager(
      LettuceConnectionFactory connectionFactory) {

    RedisClient redisClient = (RedisClient) connectionFactory.getRequiredNativeClient();

    return Bucket4jLettuce.casBasedBuilder(redisClient)
        .expirationAfterWrite(ExpirationAfterWriteStrategy.basedOnTimeForRefillingBucketUpToMax(Duration.ofMinutes(2)))
        .build();
  }

}
