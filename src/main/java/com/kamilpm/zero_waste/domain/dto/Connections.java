package com.kamilpm.zero_waste.domain.dto;

import java.util.List;

import com.kamilpm.zero_waste.domain.entity.OAuthProvider;

public record Connections(List<OAuthProvider> providers, boolean hasPassword) {

}
