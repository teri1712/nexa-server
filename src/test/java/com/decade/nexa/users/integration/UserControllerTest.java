package com.decade.nexa.users.integration;

import com.decade.nexa.common.ComponentTest;
import com.decade.nexa.common.jwt.WithJwtUser;
import com.decade.nexa.users.dto.AccountResponse;
import com.decade.nexa.users.dto.SignUpRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ComponentTest(datasets = UserDataset.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Value("${super.admin.username}")
    private String superAdminUsername;

    @Value("${super.admin.password}")
    private String superAdminPassword;

    @Test
    void givenCallerAdminExist_whenRegisterNewAdmin_thenReturnTheSubmitedInformation() throws Exception {
        // 1. Login as root user to get token
        MvcResult loginResult = mockMvc.perform(post("/login")
                .param("username", superAdminUsername)
                .param("password", superAdminPassword))
            .andExpect(status().isOk())
            .andReturn();

        AccountResponse account = objectMapper.readValue(loginResult.getResponse().getContentAsString(), AccountResponse.class);
        String token = account.getAccessToken().accessToken();

        // 2. Use token as caller to register new admin
        SignUpRequest request = new SignUpRequest();
        request.setUsername("newadmin");
        request.setPassword("Password123!");
        request.setName("New Admin");
        request.setGender(1.0f);
        request.setDob(LocalDate.now().minusYears(20));

        mockMvc.perform(post("/admins")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.username").value("newadmin"))
            .andExpect(jsonPath("$.name").value("New Admin"));
    }

    @Test
    @WithJwtUser(username = "unknown", id = "22222222-2222-2222-2222-222222222222")
    void givenCallerDoesNotExist_whenRegisterNewAdmin_thenReturn403() throws Exception {
        SignUpRequest request = new SignUpRequest();
        request.setUsername("newadmin");
        request.setPassword("Password123!");
        request.setName("New Admin");
        request.setGender(1.0f);
        request.setDob(LocalDate.now().minusYears(20));

        mockMvc.perform(post("/admins")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isForbidden());
    }
}
