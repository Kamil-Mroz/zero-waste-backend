package com.kamilpm.zero_waste.common.config;

import java.time.Duration;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJacksonJsonRedisSerializer;
import org.springframework.data.redis.serializer.JacksonJsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import com.kamilpm.zero_waste.category.api.CategoryTreeDto;

import tools.jackson.databind.JavaType;
import tools.jackson.databind.json.JsonMapper;

@Configuration
@EnableCaching
public class RedisConfig {
  @Bean
  RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory factory) {
    RedisTemplate<String, Object> template = new RedisTemplate<>();
    template.setConnectionFactory(factory);
    template.setKeySerializer(new StringRedisSerializer());
    template.setValueSerializer(GenericJacksonJsonRedisSerializer.builder().build());
    template.afterPropertiesSet();
    return template;
  }

  @Bean
  RedisCacheManager cacheManager(RedisConnectionFactory factory) {
    RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
        .entryTtl(Duration.ofHours(1))
        .serializeKeysWith(
            RedisSerializationContext.SerializationPair.fromSerializer(
                new StringRedisSerializer()))
        .disableCachingNullValues();

    JsonMapper mapper = JsonMapper.builder().build();

    JavaType descendantsType = mapper.getTypeFactory()
        .constructCollectionType(Set.class, UUID.class);

    JacksonJsonRedisSerializer<Set<UUID>> descendantsSerializer = new JacksonJsonRedisSerializer<>(descendantsType);

    JavaType treeType = mapper.getTypeFactory()
        .constructCollectionType(List.class, CategoryTreeDto.class);

    JacksonJsonRedisSerializer<List<CategoryTreeDto>> treeSerializer = new JacksonJsonRedisSerializer<>(treeType);

    RedisCacheConfiguration treeConfig = defaultConfig.serializeValuesWith(
        RedisSerializationContext.SerializationPair.fromSerializer(
            treeSerializer));

    RedisCacheConfiguration descendantsConfig = defaultConfig.serializeValuesWith(
        RedisSerializationContext.SerializationPair.fromSerializer(
            descendantsSerializer));

    return RedisCacheManager.builder(factory)
        .withCacheConfiguration(
            "categoryTree",
            treeConfig)
        .withCacheConfiguration(
            "categoryDescendants",
            descendantsConfig)
        .cacheDefaults(defaultConfig)
        .build();
  }

}
