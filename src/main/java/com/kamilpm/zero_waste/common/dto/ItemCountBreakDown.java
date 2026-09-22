package com.kamilpm.zero_waste.common.dto;

public record ItemCountBreakDown(
    long totalItems,
    long given,
    long pending,
    long available) {

}
