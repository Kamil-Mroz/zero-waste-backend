package com.kamilpm.zero_waste.image.config;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class ImageCacheConfig {
  @Bean
  @Qualifier("imageCacheManager")
  public RedisCacheManager imageCacheManager(
      RedisConnectionFactory factory) {

    RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
        .entryTtl(Duration.ofMinutes(30))
        .serializeKeysWith(
            RedisSerializationContext.SerializationPair
                .fromSerializer(new StringRedisSerializer()))
        .serializeValuesWith(
            RedisSerializationContext.SerializationPair
                .fromSerializer(new StringRedisSerializer()))
        .disableCachingNullValues();

    return RedisCacheManager.builder(factory)
        .cacheDefaults(config)
        .build();
  }
}
