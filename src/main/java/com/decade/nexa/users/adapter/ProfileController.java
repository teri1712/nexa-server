package com.decade.nexa.users.adapter;

import com.decade.nexa.users.adapter.validation.StrongPassword;
import com.decade.nexa.users.application.ports.in.ProfileService;
import com.decade.nexa.users.domain.WrongPasswordException;
import com.decade.nexa.users.dto.ProfileResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/profiles/me")
@Validated
@AllArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class ProfileController {

    private final ProfileService profileService;

    @ExceptionHandler(WrongPasswordException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ProblemDetail handleWrongPasswordException(WrongPasswordException e) {
        log.warn("Wrong password", e);
        ProblemDetail pd = ProblemDetail.forStatus(400);
        pd.setTitle("Confirm password constraint");
        pd.setDetail("Wrong password");
        return pd;
    }


    @GetMapping
    @Operation(description = "Get user profile information",
        responses = {
            @ApiResponse(responseCode = "200", description = "User profile information"),
        })
    ProfileResponse get(Principal principal) {
        return profileService.findByUsername(principal.getName());
    }


    @PostMapping("/password")
    @Operation(summary = "Change password", description = "Intended for admin",
        responses = {
            @ApiResponse(responseCode = "403", description = "The user is not an admin", content = @Content()),
            @ApiResponse(responseCode = "400", description = "Request validation or Wrong confirm password", content = @Content(
                mediaType = "application/problem+json",
                schema = @Schema(implementation = ProblemDetail.class),
                examples = {
                    @ExampleObject(name = "Request validation", ref = "#/components/examples/Validation"),
                    @ExampleObject(name = "Password mismatched", value = """
                        {
                              "title": "Confirm password constraint",
                              "status": 400,
                              "detail": "Wrong password",
                              "instance": "/profiles/me/password"
                        }
                        """)
                }
            )),
            @ApiResponse(responseCode = "200", description = "Password is changed, returning profile")
        })
    ProfileResponse changePassword(
        @AuthenticationPrincipal(expression = "id") UUID id,
        @RequestParam(value = "password", required = false) String password,
        @StrongPassword @RequestParam("new_password") String newPassword
    ) throws WrongPasswordException {
        return profileService.changeAdminPassword(id, newPassword, password);
    }
}