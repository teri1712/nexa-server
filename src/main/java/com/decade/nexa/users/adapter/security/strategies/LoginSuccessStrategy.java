package com.decade.nexa.users.adapter.security.strategies;

import com.decade.nexa.users.application.ports.in.TokenSessionService;
import com.decade.nexa.users.dto.AccountResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@AllArgsConstructor
public class LoginSuccessStrategy implements AuthenticationSuccessHandler {

      private final TokenSessionService tokenSessionService;
      private final ObjectMapper objectMapper = new ObjectMapper();

      @Override
      public void onAuthenticationSuccess(
                HttpServletRequest httpRequest,
                HttpServletResponse httpResponse,
                Authentication authentication
      ) throws IOException {

            AccountResponse accountResponse = tokenSessionService.login(authentication.getName());
            httpResponse.setContentType("application/json;charset=UTF-8");
            httpResponse.getWriter().write(objectMapper.writeValueAsString(accountResponse));
            httpResponse.setStatus(HttpServletResponse.SC_OK);
            httpResponse.getWriter().flush();
      }
}
