package com.kamilpm.zero_waste.domain.dto;

public record GithubEmail(
    String email,
    boolean primary,
    boolean verified,
    String visibility) {
}
