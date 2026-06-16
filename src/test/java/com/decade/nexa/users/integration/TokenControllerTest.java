package com.decade.nexa.users.integration;

import com.decade.nexa.common.ComponentTest;
import com.decade.nexa.common.security.UserClaims;
import com.decade.nexa.common.security.jwt.JwtUser;
import com.decade.nexa.common.security.jwt.JwtUserAuthentication;
import com.decade.nexa.users.application.ports.out.UserRepository;
import com.decade.nexa.users.domain.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ComponentTest(datasets = UserDataset.class)
class TokenControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Test
    void givenValidJwt_whenExchangeForToken_mustReturnTokenAndProfileMatchMyName() throws Exception {
        mockMvc.perform(post("/tokens/oauth2")
                .with(jwt().jwt(builder -> builder
                    .subject("google-user-123")
                    .claim("name", "Google User")
                    .claim("picture", "http://picture.com")
                )))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.profile.username").value("google-user-123"))
            .andExpect(jsonPath("$.profile.name").value("Google User"))
            .andExpect(jsonPath("$.accessToken").exists());
    }

    @Test
    void giveValidTokenWhenIAlrExchange_whenIGetMyProfile_thenMustReturnMyProfileMatchingMyName() throws Exception {
        // 1. Exchange
        mockMvc.perform(post("/tokens/oauth2")
                .with(jwt().jwt(builder -> builder
                    .subject("google-user-456")
                    .claim("name", "Another User")
                    .claim("picture", "http://picture.com")
                )))
            .andExpect(status().isOk());

        // 2. Get profile
        User user = userRepository.findByUsername("google-user-456").orElseThrow();
        UserClaims claims = new UserClaims(user.getId(), user.getUsername(), user.getName(), user.getRole().name());
        JwtUser jwtUser = new JwtUser(claims);
        Authentication auth = new JwtUserAuthentication(jwtUser, "dummy-token");

        mockMvc.perform(get("/profiles/me")
                .with(authentication(auth)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.username").value("google-user-456"))
            .andExpect(jsonPath("$.name").value("Another User"));
    }
}
