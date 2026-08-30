package com.kamilpm.zero_waste.service;

import java.time.temporal.ChronoUnit;

public interface RateLimitService {

  public void check(String action, long limit, long window, ChronoUnit unit);

}
