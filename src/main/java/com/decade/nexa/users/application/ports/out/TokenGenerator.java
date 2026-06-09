package com.decade.nexa.users.application.ports.out;

import com.decade.nexa.common.security.UserClaims;
import com.decade.nexa.users.dto.AccessToken;
import io.jsonwebtoken.JwtException;

public interface TokenGenerator {
    AccessToken generate(UserClaims userClaims);

    String generateRefreshToken(UserClaims userClaims);

    UserClaims decode(String token) throws JwtException;
}
