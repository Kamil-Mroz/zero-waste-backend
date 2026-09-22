package com.kamilpm.zero_waste.auth.dto;

import java.util.List;

import com.kamilpm.zero_waste.auth.entity.OAuthProvider;

public record Connections(List<OAuthProvider> providers, boolean hasPassword) {

}
