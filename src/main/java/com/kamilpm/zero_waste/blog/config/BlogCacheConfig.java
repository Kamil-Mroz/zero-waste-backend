package com.kamilpm.zero_waste.blog.config;

import java.time.Duration;
import java.util.List;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.JacksonJsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import com.kamilpm.zero_waste.blog.dto.BlogDto;

import tools.jackson.databind.JavaType;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.type.TypeFactory;

@Configuration
public class BlogCacheConfig {
  @Bean
  @Qualifier(value = "blogCacheManager")
  public RedisCacheManager blogCacheManager(RedisConnectionFactory factory) {

    RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
        .entryTtl(Duration.ofHours(1))
        .serializeKeysWith(
            RedisSerializationContext.SerializationPair.fromSerializer(
                new StringRedisSerializer()))
        .disableCachingNullValues();

    JsonMapper mapper = JsonMapper.builder().build();
    TypeFactory typeFactory = mapper.getTypeFactory();
    JavaType blogListType = typeFactory.constructCollectionType(List.class, BlogDto.class);

    JacksonJsonRedisSerializer<List<BlogDto>> blogListSerializer = new JacksonJsonRedisSerializer<>(blogListType);
    JacksonJsonRedisSerializer<BlogDto> blogSerializer = new JacksonJsonRedisSerializer<>(BlogDto.class);

    RedisCacheConfiguration blogListConfig = defaultConfig
        .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(blogListSerializer));
    RedisCacheConfiguration blogConfig = defaultConfig
        .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(blogSerializer));

    return RedisCacheManager.builder(factory)
        .withCacheConfiguration(
            "blogs",
            blogListConfig)
        .withCacheConfiguration(
            "blogById",
            blogConfig)
        .cacheDefaults(defaultConfig)
        .build();
  }

  @Bean
  public RedisTemplate<String, BlogDto> blogRedisTemplate(RedisConnectionFactory connectionFactory,
      ObjectMapper objectMapper) {
    RedisTemplate<String, BlogDto> template = new RedisTemplate<>();
    template.setConnectionFactory(connectionFactory);

    template.setKeySerializer(new StringRedisSerializer());
    template.setHashKeySerializer(new StringRedisSerializer());

    JacksonJsonRedisSerializer<BlogDto> serializer = new JacksonJsonRedisSerializer<>(BlogDto.class);

    template.setValueSerializer(serializer);
    template.setHashValueSerializer(serializer);

    template.afterPropertiesSet();
    return template;
  }
}
