package com.kamilpm.zero_waste.item.dto;

public record ItemCountBreakDown(
    long totalItems,
    long given,
    long pending,
    long available) {

}
