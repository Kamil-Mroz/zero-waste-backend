package com.kamilpm.zero_waste.auth.dto;

public record GithubEmail(
    String email,
    boolean primary,
    boolean verified,
    String visibility) {
}
