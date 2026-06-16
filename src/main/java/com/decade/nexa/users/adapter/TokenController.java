package com.decade.nexa.users.adapter;

import com.decade.nexa.users.application.ports.in.ProfileService;
import com.decade.nexa.users.application.ports.in.SessionService;
import com.decade.nexa.users.dto.AccountResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
public class TokenController {

    final SessionService sessionService;
    final ProfileService profileService;

    @PostMapping("/tokens/oauth2")
    AccountResponse exchange(@AuthenticationPrincipal Jwt jwt) {

        String username = jwt.getSubject();

        var claims = jwt.getClaims();
        String name = claims.get("name").toString();
        String picture = claims.get("picture").toString();

        try {
            profileService.createUser(username, name);
        } catch (Exception e) {
            log.debug("Error while exchanging token", e);
        }
        return sessionService.login(username);
    }

}
