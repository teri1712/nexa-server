package com.decade.nexa.common.security;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
public record UserClaims(
    UUID id,
    String username,
    String name,
    String role
) {

}