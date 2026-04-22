package com.decade.nexa.users.adapter;

import com.decade.nexa.users.application.ports.in.ProfileService;
import com.decade.nexa.users.application.ports.in.TokenSessionService;
import com.decade.nexa.users.domain.NeedAParentAdminException;
import com.decade.nexa.users.dto.AccountResponse;
import com.decade.nexa.users.dto.ProfileResponse;
import com.decade.nexa.users.dto.SignUpRequest;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Slf4j
@RestController
@AllArgsConstructor
public class UserController {

      private final ProfileService profileService;
      private final TokenSessionService tokenSessionService;

      @ExceptionHandler(DataIntegrityViolationException.class)
      @ResponseStatus(HttpStatus.CONFLICT)
      public ProblemDetail handleException(DataIntegrityViolationException ex) {
            log.debug("Integrity violation", ex);
            ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.CONFLICT);
            pd.setDetail("Username already exists");
            return pd;
      }

      @ExceptionHandler(NeedAParentAdminException.class)
      @ResponseStatus(HttpStatus.UNAUTHORIZED)
      public ProblemDetail handleException(NeedAParentAdminException ex) {
            log.debug("Require admin authentication", ex);
            ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.UNAUTHORIZED);
            pd.setDetail("Require admin authentication");
            return pd;
      }

      @PostMapping("/admins")
      @ResponseStatus(HttpStatus.CREATED)
      public ProfileResponse registerAdmin(@RequestBody @Valid SignUpRequest signUpRequest, @AuthenticationPrincipal(expression = "id") UUID caller) {
            return profileService.create(signUpRequest, caller);
      }


      @PostMapping("/user-login")
      public AccountResponse exchange(
                @AuthenticationPrincipal Jwt jwt
      ) throws Exception {
            return tokenSessionService.loginOauth(jwt);
      }


}