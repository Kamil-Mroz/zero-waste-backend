package com.kamilpm.zero_waste.service.impl;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import org.springframework.stereotype.Service;

import com.kamilpm.zero_waste.domain.entity.User;
import com.kamilpm.zero_waste.exception.RateLimitException;
import com.kamilpm.zero_waste.service.AuthService;
import com.kamilpm.zero_waste.service.RateLimitService;

import io.github.bucket4j.Bucket;
import io.github.bucket4j.BucketConfiguration;
import io.github.bucket4j.ConsumptionProbe;
import io.github.bucket4j.distributed.proxy.ProxyManager;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RateLimitServiceImpl implements RateLimitService {
  private final AuthService authService;

  private final ProxyManager<byte[]> proxyManager;

  @Override
  public void check(String action, long limit, long window, ChronoUnit unit) {
    User user = authService.getRequiredAuthenticatedUser();
    String key = "rate-limit:user:" + user.getId() + ":" + action;

    byte[] bucketKey = key.getBytes(StandardCharsets.UTF_8);

    Duration duration = unit.getDuration().multipliedBy(window);

    BucketConfiguration configuration = BucketConfiguration.builder()
        .addLimit(limitConfig -> limitConfig.capacity(limit).refillGreedy(limit, duration)).build();

    Bucket bucket = proxyManager.builder().build(bucketKey, () -> configuration);

    ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);
    if (!probe.isConsumed()) {
      Duration retryAfter = Duration.ofNanos(probe.getNanosToWaitForRefill());
      throw new RateLimitException(retryAfter);
    }

  }
}
