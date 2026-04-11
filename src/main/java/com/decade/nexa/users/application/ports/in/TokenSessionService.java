package com.decade.nexa.users.application.ports.in;

import com.decade.nexa.users.dto.AccountResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.oauth2.jwt.Jwt;

public interface TokenSessionService {

      AccountResponse login(String username);

      AccountResponse loginOauth(Jwt jwt);

      String refresh(String refreshToken) throws AccessDeniedException;

      void logout(String username, String refreshToken);

}
