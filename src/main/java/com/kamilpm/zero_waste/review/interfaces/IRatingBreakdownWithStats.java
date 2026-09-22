package com.kamilpm.zero_waste.review.interfaces;

public interface IRatingBreakdownWithStats {

  Integer getRating();

  Long getCount();

  Double getAvgRating();

  Long getTotalCount();

}
