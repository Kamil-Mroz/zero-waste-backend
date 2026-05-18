package com.kamilpm.zero_waste.utils;

public final class SqlUtils {
  private SqlUtils() {

  }

  public static String escapeLike(String input) {
    if (input == null) {
      return null;
    }

    return input
        .replace("\\", "\\\\")
        .replace("%", "\\%")
        .replace("_", "\\_");
  }

  public static String prepareLikePattern(String input) {
    if (input == null || input.isBlank()) {
      return null;
    }
    return "%" + escapeLike(input.trim().toLowerCase()) + "%";
  }

}
