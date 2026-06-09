package com.decade.nexa.common.security;

import java.time.Duration;

public interface TokenService {

    UserClaims decodeToken(String token);

    String encodeToken(UserClaims userClaims, Duration duration);

}
