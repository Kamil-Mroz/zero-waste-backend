package com.kamilpm.zero_waste.auth.dto;

public record GithubUserInfo(
    Long id,
    String login,
    String name,
    String email) {
}
