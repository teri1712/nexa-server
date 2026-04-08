package com.decade.nexa.bdd.steps;


import com.decade.nexa.bdd.context.AuthContext;
import com.decade.nexa.bdd.context.SignUpContext;
import com.decade.nexa.users.dto.ProfileResponse;
import io.cucumber.java.Before;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;

import java.time.Instant;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@RequiredArgsConstructor
public class SignupSteps {

      private final SignUpContext signUpContext;
      private final Environment environment;
      private final AuthContext authContext;

      @Before
      public void setup() {
            RestAssured.port = Integer.parseInt(environment.getProperty("local.server.port"));
            RestAssured.baseURI = "http://localhost";
      }

      @When("sign up new account with username {string} and password {string} by admin {string} - {string}")
      public void signUp(String username, String password, String admin, String adminPassword) {
            Response response = RestAssured.given().contentType("application/json")
                      .auth().preemptive().basic(admin, adminPassword)
                      .body(
                                Map.of("username", username,
                                          "name", username,
                                          "dob", Instant.now(),
                                          "gender", 0.5,
                                          "password", password))
                      .post("/admins");
            signUpContext.status = response.statusCode();
            if (response.statusCode() != 201) {
                  signUpContext.errorMessage = response.jsonPath().getString("detail");
                  log.error("Error: {} {}", response.statusCode(), response.body().prettyPrint());
            } else {
                  signUpContext.profile = response.jsonPath().getObject(".", ProfileResponse.class);
            }

      }

      @Then("fails with error {string}")
      public void failsWithError(String error) {
            assertThat(signUpContext.errorMessage).isEqualTo(error);
      }

      @When("the admin sign up a new admin account with username {string} and password {string}")
      public void signUpAdmin(String username, String password) {
            Response response = RestAssured.given()
                      .headers("Authorization", "Bearer " + authContext.accessToken.accessToken())

                      .contentType("application/json")
                      .body(
                                Map.of("username", username,
                                          "name", username,
                                          "dob", Instant.now(),
                                          "gender", 0.5,
                                          "password", password))
                      .post("/admins");
            signUpContext.status = response.statusCode();
            if (response.statusCode() != 201) {
                  signUpContext.errorMessage = response.jsonPath().getString("detail");
                  log.error("Error: {} {}", response.statusCode(), response.body().prettyPrint());
            } else {
                  signUpContext.profile = response.jsonPath().getObject(".", ProfileResponse.class);
            }

      }
}
