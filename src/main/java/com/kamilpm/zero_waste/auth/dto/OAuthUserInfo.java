package com.kamilpm.zero_waste.auth.dto;

import com.kamilpm.zero_waste.auth.entity.OAuthProvider;

public record OAuthUserInfo(OAuthProvider provider, String providerId, String email, String nickname) {

}
