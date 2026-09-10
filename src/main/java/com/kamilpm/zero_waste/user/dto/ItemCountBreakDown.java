package com.kamilpm.zero_waste.user.dto;

public record ItemCountBreakDown(
    long totalItems,
    long given,
    long pending,
    long available) {

}
