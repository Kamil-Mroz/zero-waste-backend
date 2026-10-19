package com.kamilpm.zero_waste.ratelimit;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import com.kamilpm.zero_waste.common.annotation.RateLimit;


@Aspect
@Component
public class RateLimitAspect {
  private final RateLimitService rateLimitService;



  public RateLimitAspect(RateLimitService rateLimitService) {
    this.rateLimitService = rateLimitService;
  }



  @Around("@annotation(rateLimit)")
  public Object checkRateLimit(ProceedingJoinPoint joinPoint, RateLimit rateLimit) throws Throwable {

    rateLimitService.check(rateLimit.action(), rateLimit.limit(),
        rateLimit.window(), rateLimit.unit());
    return joinPoint.proceed();
  }

}
