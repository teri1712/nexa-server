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

import java.io.IOException;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.notNullValue;

@RequiredArgsConstructor
public class UploadSteps {

      private final AuthContext authContext;
      private final UploadContext uploadContext;
      private final Environment environment;

      @Before
      public void setup() {
            RestAssured.port = Integer.parseInt(environment.getProperty("local.server.port"));
            RestAssured.baseURI = "http://localhost";
      }

      @When("user uploading a pdf file at {string}")
      public void whenUpload(String fileName) throws IOException {
            Response response = RestAssured.given()
                      .headers("Authorization", "Bearer " + authContext.accessToken.accessToken())
                      .queryParam("filename", fileName)
                      .post("/files/upload")
                      .andReturn();
            response.then().statusCode(200)
                      .body("presignedUploadUrl", notNullValue())
                      .body("key", notNullValue());

            String uploadUrl = response.jsonPath().getString("presignedUploadUrl");
            String key = response.jsonPath().getString("key");
            uploadContext.key = key;
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

            response = RestAssured.given()
                      .contentType("application/json")
                      .headers("Authorization", "Bearer " + authContext.accessToken.accessToken())
                      .body(Map.of("key", key, "eTag", eTag))
                      .post("/files/finish");
            uploadContext.finishStatus = response.statusCode();
            uploadContext.downloadUrl = response.body().asString();
      }

      @Then("the document is saved")
      public void theDocumentIsSaved() {
            assertThat(uploadContext.downloadUrl).isNotBlank();
            assertThat(uploadContext.finishStatus).isEqualTo(200);
      }
}
