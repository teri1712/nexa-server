package com.decade.nexa.users.integration;

import com.decade.nexa.common.ComponentTest;
import com.decade.nexa.common.security.UserClaims;
import com.decade.nexa.common.security.jwt.JwtUser;
import com.decade.nexa.common.security.jwt.JwtUserAuthentication;
import com.decade.nexa.users.application.ports.out.UserRepository;
import com.decade.nexa.users.domain.User;
import com.decade.nexa.users.dto.ProfileResponse;
import com.decade.nexa.users.dto.SignUpRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDate;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ComponentTest(datasets = UserDataset.class)
class ProfileControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Value("${super.admin.username}")
    private String superAdminUsername;

    @Test
    void givenAdminExist_whenGettingHisProfile_thenMustReturnHisProfile() throws Exception {
        // 1. Setup super admin as caller
        User superAdmin = userRepository.findByUsername(superAdminUsername).orElseThrow();
        Authentication superAuth = createAuth(superAdmin);

        // 2. Register new admin via UserController
        SignUpRequest request = new SignUpRequest();
        request.setUsername("admin2");
        request.setPassword("Password123!");
        request.setName("Admin Two");
        request.setGender(1.0f);
        request.setDob(LocalDate.now().minusYears(25));

        MvcResult result = mockMvc.perform(post("/admins")
                .with(authentication(superAuth))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andReturn();

        ProfileResponse registeredAdmin = objectMapper.readValue(result.getResponse().getContentAsString(), ProfileResponse.class);

        // 3. Get profile as the new admin
        User newAdmin = userRepository.findByUsername("admin2").orElseThrow();
        Authentication adminAuth = createAuth(newAdmin);

        mockMvc.perform(get("/profiles/me")
                .with(authentication(adminAuth)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.username").value("admin2"))
            .andExpect(jsonPath("$.name").value("Admin Two"));
    }

    @Test
    void givenCorrectPassword_whenChangePassword_thenReturn200() throws Exception {
        // 1. Setup an admin
        User superAdmin = userRepository.findByUsername(superAdminUsername).orElseThrow();
        Authentication superAuth = createAuth(superAdmin);

        SignUpRequest request = new SignUpRequest();
        request.setUsername("admin3");
        request.setPassword("OldPassword123!");
        request.setName("Admin Three");
        request.setGender(1.0f);
        request.setDob(LocalDate.now().minusYears(25));

        mockMvc.perform(post("/admins")
                .with(authentication(superAuth))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated());

        User admin = userRepository.findByUsername("admin3").orElseThrow();
        Authentication adminAuth = createAuth(admin);

        // 2. Change password with correct old password
        mockMvc.perform(post("/profiles/me/password")
                .with(authentication(adminAuth))
                .param("password", "OldPassword123!")
                .param("new_password", "NewPassword123!"))
            .andExpect(status().isOk());
    }

    @Test
    void givenIncorrectPassword_whenChangePassword_thenReturn400() throws Exception {
        // 1. Setup an admin
        User superAdmin = userRepository.findByUsername(superAdminUsername).orElseThrow();
        Authentication superAuth = createAuth(superAdmin);

        SignUpRequest request = new SignUpRequest();
        request.setUsername("admin4");
        request.setPassword("OldPassword123!");
        request.setName("Admin Four");
        request.setGender(1.0f);
        request.setDob(LocalDate.now().minusYears(25));

        mockMvc.perform(post("/admins")
                .with(authentication(superAuth))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated());

        User admin = userRepository.findByUsername("admin4").orElseThrow();
        Authentication adminAuth = createAuth(admin);

        // 2. Change password with incorrect old password
        mockMvc.perform(post("/profiles/me/password")
                .with(authentication(adminAuth))
                .param("password", "WrongPassword!")
                .param("new_password", "NewPassword123!"))
            .andExpect(status().isBadRequest());
    }

    private Authentication createAuth(User user) {
        UserClaims claims = new UserClaims(user.getId(), user.getUsername(), user.getName(), user.getRole().name());
        JwtUser jwtUser = new JwtUser(claims);
        return new JwtUserAuthentication(jwtUser, "dummy-token");
    }
}
