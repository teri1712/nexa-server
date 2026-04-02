package com.decade.nexa.users.application.ports.in;

import com.decade.nexa.users.dto.AccountResponse;
import org.springframework.security.access.AccessDeniedException;

public interface TokenSessionService {

      AccountResponse login(String username);

      String refresh(String refreshToken) throws AccessDeniedException;

      void logout(String username, String refreshToken);

}
