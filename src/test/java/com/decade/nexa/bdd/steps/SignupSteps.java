package com.decade.nexa.bdd.steps;


import com.decade.nexa.bdd.context.SignUpContext;
import com.decade.nexa.users.dto.ProfileResponse;
import io.cucumber.java.Before;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import lombok.RequiredArgsConstructor;
import org.springframework.core.env.Environment;

import java.time.Instant;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@RequiredArgsConstructor
public class SignupSteps {

      private final SignUpContext signUpContext;
      private final Environment environment;

      @Before
      public void setup() {
            RestAssured.port = Integer.parseInt(environment.getProperty("local.server.port"));
            RestAssured.baseURI = "http://localhost";
      }

      @When("user sign up new account with username {string} and password {string}")
      public void signUp(String username, String password) {
            Response response = RestAssured.given().contentType("application/json")
                      .body(
                                Map.of("username", username,
                                          "name", username,
                                          "dob", Instant.now(),
                                          "gender", 0.5,
                                          "password", password))
                      .post("/users");
            signUpContext.profile = response.jsonPath().getObject(".", ProfileResponse.class);
            signUpContext.status = response.statusCode();
            if (response.statusCode() != 201) {
                  signUpContext.errorMessage = response.jsonPath().getString("detail");
            }

      }

      @Then("fails with error {string}")
      public void failsWithError(String error) {
            assertThat(signUpContext.errorMessage).isEqualTo(error);
      }
}
