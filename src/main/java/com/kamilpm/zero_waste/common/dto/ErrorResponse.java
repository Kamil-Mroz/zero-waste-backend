package com.kamilpm.zero_waste.common.dto;

public record ErrorResponse(
    String detail,
    String instance,
    int status,
    String title) {

}
