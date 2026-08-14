package com.kamilpm.zero_waste.domain.dto;

public record GithubUserInfo(
    Long id,
    String login,
    String name,
    String email) {
}
