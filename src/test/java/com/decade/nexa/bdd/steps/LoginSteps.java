package com.decade.nexa.bdd.steps;

import com.decade.nexa.bdd.context.AuthContext;
import com.decade.nexa.users.dto.AccessToken;
import com.decade.nexa.users.dto.ProfileResponse;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class LoginSteps {

      private final AuthContext authContext;

      @LocalServerPort
      private int port;

      @Before
      void setup() {
            RestAssured.port = port;
            RestAssured.baseURI = "http://localhost";
      }

      @Given("user exist with username {string} and password {string}")
      void userExists(String username, String password) {
            Response response = RestAssured.given().contentType("application/json")
                      .body(Map.of("username", username, "password", password))
                      .post("/login");
            authContext.statusCode = response.statusCode();
            authContext.accessToken = response.jsonPath().getObject("accessToken", AccessToken.class);
            authContext.profile = response.jsonPath().getObject("profile", ProfileResponse.class);
      }

      @When("user logins with username {string} and password {string}")
      void login(String username, String password) {
            Response response = RestAssured.given().contentType("application/json")
                      .body(Map.of("username", username, "password", password))
                      .post("/login");
            authContext.statusCode = response.statusCode();
            authContext.accessToken = response.jsonPath().getObject("accessToken", AccessToken.class);
            authContext.profile = response.jsonPath().getObject("profile", ProfileResponse.class);
      }
}
