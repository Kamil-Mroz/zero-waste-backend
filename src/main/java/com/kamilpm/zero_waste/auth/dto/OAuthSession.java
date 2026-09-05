package com.kamilpm.zero_waste.auth.dto;

import java.util.UUID;

import com.kamilpm.zero_waste.auth.entity.OAuthProvider;

public record OAuthSession(UUID userId, OAuthProvider provider, String state, OAuthFlow flow) {

}
