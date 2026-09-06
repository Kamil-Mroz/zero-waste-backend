package com.kamilpm.zero_waste.common.ratelimit;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import org.springframework.stereotype.Service;

import com.kamilpm.zero_waste.auth.api.AuthApi;
import com.kamilpm.zero_waste.auth.api.AuthenticatedUser;
import com.kamilpm.zero_waste.common.exception.RateLimitException;

import io.github.bucket4j.Bucket;
import io.github.bucket4j.BucketConfiguration;
import io.github.bucket4j.ConsumptionProbe;
import io.github.bucket4j.distributed.proxy.ProxyManager;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RateLimitService {
  private final AuthApi authApi;

  private final ProxyManager<byte[]> proxyManager;

  public void check(String action, long limit, long window, ChronoUnit unit) {
    AuthenticatedUser user = authApi.getRequiredAuthenticatedUser();
    String key = "rate-limit:user:" + user.id() + ":" + action;

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
