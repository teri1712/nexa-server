package com.decade.nexa.users.adapter;

import com.decade.nexa.users.application.ports.in.ProfileService;
import com.decade.nexa.users.application.ports.in.SessionService;
import com.decade.nexa.users.domain.InvalidTokenException;
import com.decade.nexa.users.domain.NeedAParentAdminException;
import com.decade.nexa.users.domain.UserAlreadyExistException;
import com.decade.nexa.users.dto.AccountResponse;
import com.decade.nexa.users.dto.ProfileResponse;
import com.decade.nexa.users.dto.SignUpRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Slf4j
@RestController
@AllArgsConstructor
public class UserController {

    private final ProfileService profileService;
    private final SessionService sessionService;


    @ExceptionHandler(InvalidTokenException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    ProblemDetail handle(InvalidTokenException ex) {
        log.debug("Invalid token", ex);
        ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.UNAUTHORIZED);
        pd.setTitle("Token validation");
        pd.setDetail("Invalid token");
        return pd;
    }

    @ExceptionHandler(UserAlreadyExistException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    ProblemDetail handle(UserAlreadyExistException ex) {
        log.debug("User already exist", ex);
        ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        pd.setTitle("Username constraint");
        pd.setDetail("User already exist");
        return pd;
    }

    @ExceptionHandler(NeedAParentAdminException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    void handle(NeedAParentAdminException ex) {
        log.debug("Require admin authentication", ex);
    }

    @PostMapping("/admins")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Register new admin", description = "Require an admin authentication",
        responses = {
            @ApiResponse(responseCode = "201", description = "New admin is created"),
            @ApiResponse(responseCode = "403", description = "Require an admin authentication", content = @Content()),
            @ApiResponse(responseCode = "400", description = "Username exist constraint or validation error",
                content = @Content(
                    mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class),
                    examples = {
                        @ExampleObject(name = "User already exist", value = """
                            {
                                  "title": "Username constraint",
                                  "status": 400,
                                  "detail": "User already exist",
                                  "instance": "/admins"
                            }
                            """),
                        @ExampleObject(name = "Request validation", ref = "#/components/examples/Validation")

                    }
                )),
        })
    @ResponseStatus(HttpStatus.CREATED)
    ProfileResponse registerAdmin(
        @RequestBody @Valid SignUpRequest signUpRequest,
        @AuthenticationPrincipal(expression = "id") UUID caller)
        throws NeedAParentAdminException, UserAlreadyExistException {
        return profileService.create(signUpRequest, caller);
    }


    @PostMapping("/login")
    @Operation(summary = "Login for admin", responses = {
        @ApiResponse(responseCode = "200", description = "Successful operation"),
        @ApiResponse(responseCode = "401", description = "Invalid username or password", content = @Content(
            schema = @Schema(implementation = ProblemDetail.class),
            examples = {
                @ExampleObject(
                    name = "Wrong password",
                    value = """
                                  "title": "Authentication Failed",
                                  "status": 401,
                                  "detail": "Wrong password",
                                  "instance": "/login"
                        """),
                @ExampleObject(
                    name = "Username not found",
                    value = """
                                  "title": "Authentication Failed",
                                  "status": 401,
                                  "detail": "Username not found",
                                  "instance": "/login"
                        """)
            }
        ))
    })
    AccountResponse loginAdmin(@RequestParam String username, @RequestParam String password) {
        return sessionService.login(username);
    }


}