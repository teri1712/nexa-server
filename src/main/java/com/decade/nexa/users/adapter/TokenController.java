package com.decade.nexa.users.adapter;

import com.decade.nexa.users.application.ports.in.SessionService;
import com.decade.nexa.users.domain.ExpiredTokenException;
import com.decade.nexa.users.dto.AccessToken;
import com.decade.nexa.users.dto.AccountResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.*;

import static com.decade.nexa.web.security.TokenUtils.REFRESH_PARAM;

@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping("/tokens")
public class TokenController {

      private final SessionService sessionService;

      @ExceptionHandler(ExpiredTokenException.class)
      @ResponseStatus(HttpStatus.UNAUTHORIZED)
      ProblemDetail handleExpiredTokenException(ExpiredTokenException ex) {
            log.debug("Expired token", ex);
            ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.UNAUTHORIZED);
            pd.setTitle("Token validation");
            pd.setDetail("Expired token");
            return pd;
      }

      @PostMapping("/refresh")
      @Operation(summary = "Refresh new access token", responses = {
                @ApiResponse(responseCode = "200", description = "New access token is generated"),
                @ApiResponse(responseCode = "401", description = "Expired token", content = @Content(
                          mediaType = "application/problem+json",
                          schema = @Schema(implementation = ProblemDetail.class),
                          examples = {
                                    @ExampleObject(value = """
                                              {
                                                    "title": "Token validation",
                                                    "status": 401,
                                                    "detail": "Expired token",
                                                    "instance": "/tokens/refresh"
                                              }
                                              """)
                          }))
      })
      AccountResponse refresh(@RequestParam(REFRESH_PARAM) String refreshToken) throws ExpiredTokenException {
            String accessToken = sessionService.refresh(refreshToken);
            return new AccountResponse(null, new AccessToken(accessToken, refreshToken));
      }
}