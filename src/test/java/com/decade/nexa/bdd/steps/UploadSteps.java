package com.decade.nexa.bdd.steps;

import com.decade.nexa.bdd.context.AuthContext;
import com.decade.nexa.bdd.context.UploadContext;
import io.cucumber.java.Before;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import lombok.RequiredArgsConstructor;
import org.springframework.core.env.Environment;

import java.util.Map;

import static org.hamcrest.Matchers.notNullValue;

@RequiredArgsConstructor
public class UploadSteps {

      private final Environment environment;

      private final AuthContext authContext;
      private final UploadContext uploadContext;
      private Response uploadResponse;

      @Before
      public void setup() {
            RestAssured.port = Integer.parseInt(environment.getProperty("local.server.port"));
            RestAssured.baseURI = "http://localhost";
      }

      @When("upload a {string} file at {string} with title {string} and description {string}")
      public void whenUpload(String type, String fileName, String title, String description) {
            uploadResponse = RestAssured.given()
                      .headers("Authorization", "Bearer " + authContext.accessToken.accessToken())
                      .queryParam("filename", fileName)

                      .when()
                      .post("/files/upload")

                      .then()
                      .statusCode(200)
                      .body("presignedUploadUrl", notNullValue())
                      .body("fileKey", notNullValue())

                      .extract()
                      .response();

            String uploadUrl = uploadResponse.jsonPath().getString("presignedUploadUrl");
            String key = uploadResponse.jsonPath().getString("fileKey");
            String eTag = RestAssured.given()
                      .urlEncodingEnabled(false)
                      .contentType(ContentType.BINARY)
                      .body(getClass().getResourceAsStream("/samples/" + fileName))
                      .put(uploadUrl)
                      .then()
                      .statusCode(200)
                      .header("ETag", notNullValue())
                      .extract()
                      .header("ETag");

            uploadResponse = RestAssured.given()
                      .contentType("application/json")
                      .headers("Authorization", "Bearer " + authContext.accessToken.accessToken())
                      .body(Map.of(
                                "key", key,
                                "filename", fileName,
                                "eTag", eTag,
                                "type", type,
                                "title", title,
                                "description", description

                      ))

                      .when()
                      .post("/docs");
            uploadContext.key = key;
            uploadContext.eTag = eTag;

      }

      @Then("the document is saved")
      public void theDocumentIsSaved() {
            uploadResponse.then()
                      .statusCode(202);
      }
}
