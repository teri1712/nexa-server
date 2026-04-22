package com.decade.nexa.bdd.steps;

import com.decade.nexa.bdd.context.AuthContext;
import io.cucumber.java.Before;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;

import java.time.Duration;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;

@Slf4j
@RequiredArgsConstructor
public class SearchSteps {

    private final AuthContext authContext;
    private final Environment environment;
    private final LoginSteps loginSteps;
    private final UploadSteps uploadSteps;

    private Response searchResponse;


    @Before
    public void setup() {
        RestAssured.port = Integer.parseInt(environment.getProperty("local.server.port"));
        RestAssured.baseURI = "http://localhost";
    }

    @When("user search with keyword {string} and content type {string}")
    public void userSearchWithKeyword(String query, String contentType) {
        searchResponse = RestAssured.given()
            .headers("Authorization", "Bearer " + authContext.accessToken.accessToken())
            .queryParam("query", query)
            .queryParam("type", contentType)

            .when()
            .get("/docs");
    }

    @Then("the document {string} must be return and that file type must be {string}")
    public void theDocumentMustBeReturn(String filename, String fileType) {
        searchResponse.then()
            .statusCode(200)
            .body("docs.size()", equalTo(1))
            .body("docs[0].filename", equalTo(filename))
            .body("docs[0].fileType", equalTo(fileType));
    }

    @When("user search with keyword {string} and date is yesterday")
    public void userSearchWithKeywordAndDateIsYesterday(String query) {
        Instant date = Instant.now().minus(Duration.ofDays(1));
        searchResponse = RestAssured
            .given()
            .headers("Authorization", "Bearer " + authContext.accessToken.accessToken())
            .queryParam("query", query)
            .queryParam("end", date.toString())
            .queryParam("type", "PDF")

            .when()
            .get("/docs");
    }

    @Then("no documents is return")
    public void noDocumentsIsReturn() {
        searchResponse.then()
            .statusCode(200)
            .body("docs.size()", equalTo(0));
    }

    private Response suggestContext;

    @When("user query {string}")
    public void userQuery(String query) {
        suggestContext = RestAssured
            .given()
            .headers("Authorization", "Bearer " + authContext.accessToken.accessToken())
            .queryParam("query", query)

            .when()
            .post("/docs/suggest");
    }

    @Then("AI suggest some information")
    public void aiSuggestSomeInformation() {
        String body = suggestContext.then()
            .contentType("text/plain")
            .statusCode(200)
            .body(not(emptyString()))
            .extract().asString();
        ;
        log.info("Suggested: {}", body);
    }

    @And("file pdf {string} is alr uploaded")
    public void fileUploaded(String fileName) {
        assertThat(fileName).isNotNull();
        loginSteps.adminExists("teri1712", "teri1712");
        loginSteps.login("teri1712", "teri1712");
        uploadSteps.whenUpload("PDF", fileName, fileName, fileName);
        loginSteps.logout();
    }
}
