package com.decade.nexa.users.adapter;

import com.decade.nexa.users.application.ports.in.TokenSessionService;
import com.decade.nexa.users.dto.AccessToken;
import com.decade.nexa.users.dto.AccountResponse;
import com.decade.nexa.web.security.TokenUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping("/tokens")
public class TokenController {

      private final TokenSessionService tokenSessionService;

      @PostMapping("/refresh")
      public AccountResponse refresh(HttpServletRequest request) {
            String refreshToken = TokenUtils.extractRefreshToken(request);
            if (refreshToken == null) {
                  throw new AccessDeniedException("No refresh token provided in the request");
            }

            String accessToken = tokenSessionService.refresh(refreshToken);
            return new AccountResponse(null, new AccessToken(accessToken, refreshToken));
      }
}