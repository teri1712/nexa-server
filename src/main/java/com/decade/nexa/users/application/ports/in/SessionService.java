package com.decade.nexa.users.application.ports.in;

import com.decade.nexa.users.domain.ExpiredTokenException;
import com.decade.nexa.users.domain.InvalidTokenException;
import com.decade.nexa.users.dto.AccountResponse;
import org.springframework.security.oauth2.jwt.Jwt;

public interface SessionService {

      AccountResponse login(String username);

      AccountResponse loginOauth(Jwt jwt) throws InvalidTokenException;

      String refresh(String refreshToken) throws ExpiredTokenException;

      void logout(String username, String refreshToken);

}
