package com.kamilpm.zero_waste.domain.interfaces;

public interface IRatingBreakdownWithStats {

  Integer getRating();

  Long getCount();

  Double getAvgRating();

  Long getTotalCount();

}
