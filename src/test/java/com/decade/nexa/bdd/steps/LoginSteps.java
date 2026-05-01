package com.decade.nexa.bdd.steps;

import com.decade.nexa.bdd.context.AuthContext;
import com.decade.nexa.bdd.context.SuperContext;
import com.decade.nexa.users.dto.AccessToken;
import com.decade.nexa.users.dto.ProfileResponse;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Date;
import java.util.Map;

import static com.decade.nexa.bdd.config.OIDCConfig.TEST_RSA_KEY;
import static com.decade.nexa.users.infra.ODIC.OIDC_HEADER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.equalTo;

@Slf4j
@RequiredArgsConstructor
public class LoginSteps {

    private final AuthContext authContext;
    private final SuperContext superContext;
    private final Environment environment;

    @Before
    public void setup() {
        RestAssured.port = Integer.parseInt(environment.getProperty("local.server.port"));
        RestAssured.baseURI = "http://localhost";
    }

    private void consumeAuth(Response response) {
        authContext.statusCode = response.statusCode();
        if (response.statusCode() != 200) {
            log.error("Error: {} {}", response.statusCode(), response.body().prettyPrint());
            authContext.errorMessage = response.jsonPath().getString("detail");
        } else {
            authContext.accessToken = response.jsonPath().getObject("accessToken", AccessToken.class);
            authContext.profile = response.jsonPath().getObject("profile", ProfileResponse.class);
        }
    }

    @When("logins with username {string} and password {string}")
    public void login(String username, String password) {
        Response response = RestAssured.given().contentType(ContentType.URLENC)
            .formParam("username", username)
            .formParam("password", password)
            .post("/login");

        consumeAuth(response);
    }

    @Then("should be granted access and their profile information")
    public void thenSuccess() {
        assertThat(authContext.statusCode).isEqualTo(200);
        assertThat(authContext.accessToken).isNotNull();
        assertThat(authContext.profile).isNotNull();
    }

    @Then("the user should be denied access with {string} message")
    public void thenDenied(String message) {
        assertThat(authContext.statusCode).isEqualTo(401);
        assertThat(authContext.errorMessage).isEqualTo(message);
    }

    @Given("admin {string} does not exist")
    public void usernameDoesNotExist(String username) {
    }

    @Given("an admin exist with username {string} and password {string}")
    public void adminExists(String username, String password) {
        RestAssured.given().contentType("application/json")
            .auth().preemptive().basic(superContext.getSuperUsername(), superContext.getSuperPassword())
            .body(
                Map.of("username", username,
                    "name", username,
                    "dob", LocalDate.now().minusDays(1),
                    "gender", 0.5,
                    "password", password))
            .post("/admins")
            .then()
            .statusCode(201)
            .body("username", equalTo(username));
    }

    public static String oidcToken(String email, String name) {
        try {
            var signer = new RSASSASigner(TEST_RSA_KEY);

            var claims = new JWTClaimsSet.Builder()
                .issuer("https://accounts.google.com")
                .subject(email)
                .claim("email", email)
                .claim("name", name)
                .audience("test-client-id")
                .issueTime(Date.from(Instant.now()))
                .expirationTime(Date.from(Instant.now().plusSeconds(3600)))
                .build();

            var header = new JWSHeader.Builder(JWSAlgorithm.RS256)
                .keyID("test-key")
                .build();

            var jwt = new SignedJWT(header, claims);
            jwt.sign(signer);
            return jwt.serialize();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private String oidcToken;

    @Given("user with email {string} and name {string} grant consent to the application")
    public void userEmailConsent(String email, String name) {
        this.oidcToken = oidcToken(email, name);
    }

    @When("user login to the application with that email")
    public void userLoginToTheApplicationWithThatEmail() {
        Response response = RestAssured.given().contentType("application/json")
            .headers(OIDC_HEADER, oidcToken)
            .post("/user-login")
            .andReturn();
        consumeAuth(response);
    }

    @Given("user {string} login")
    public void userEmailLogin(String email) {
        this.userEmailConsent(email, email);
        this.userLoginToTheApplicationWithThatEmail();
    }

    @When("logout")
    public void logout() {
        RestAssured.given()
            .contentType("application/x-www-form-urlencoded")
            .headers("Authorization", "Bearer " + authContext.accessToken.accessToken())
            .param("refresh_token", authContext.accessToken.refreshToken())
            .post("/logout")
            .then()
            .statusCode(200);
    }

}
