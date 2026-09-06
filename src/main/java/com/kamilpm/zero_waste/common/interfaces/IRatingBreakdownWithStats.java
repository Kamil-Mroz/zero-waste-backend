package com.kamilpm.zero_waste.common.interfaces;

public interface IRatingBreakdownWithStats {

  Integer getRating();

  Long getCount();

  Double getAvgRating();

  Long getTotalCount();

}
