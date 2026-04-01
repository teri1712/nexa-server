package com.decade.nexa.users.dto;

public record AccessToken(
          String accessToken,
          String refreshToken
) {
}