package com.kamilpm.zero_waste.domain.dto;

import java.util.UUID;

import com.kamilpm.zero_waste.domain.entity.OAuthProvider;

public record OAuthSession(UUID userId, OAuthProvider provider, String state, OAuthFlow flow) {

}
