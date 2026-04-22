package com.decade.nexa.bdd.steps;

import com.decade.nexa.bdd.context.AuthContext;
import com.decade.nexa.bdd.context.MessageContext;
import com.decade.nexa.messages.dto.MessageDto;
import com.decade.nexa.messages.dto.MessagePlacedDto;
import io.cucumber.java.Before;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.RestAssured;
import io.restassured.common.mapper.TypeRef;
import io.restassured.response.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.emptyString;
import static org.hamcrest.Matchers.not;

@Slf4j
@RequiredArgsConstructor
public class MessageSteps {

    private final Environment environment;
    private final AuthContext authContext;
    private final MessageContext messageContext;

    @Before
    public void setup() {
        RestAssured.port = Integer.parseInt(environment.getProperty("local.server.port"));
        RestAssured.baseURI = "http://localhost";
    }

    @And("he has {int} messages (half of them are his)")
    public void heHasMessages(int messageCount) {
        List<MessageDto> messages = new ArrayList<>();
        for (int i = 0; i < messageCount / 2; i++) {
            MessagePlacedDto response =
                RestAssured.given()
                    .headers("Authorization", "Bearer " + authContext.accessToken.accessToken())
                    .param("message", "HHeelloo" + (i + 1))

                    .when()
                    .post("/messages")

                    .then()
                    .body(not(emptyString()))
                    .statusCode(200)
                    .extract()
                    .response().as(MessagePlacedDto.class);
            messages.add(response.userMessage());
            Long agentMessageId = response.placeHolderMessage().sequenceNumber();

            String agentReply = RestAssured.given()
                .headers("Authorization", "Bearer " + authContext.accessToken.accessToken())
                .contentType("application/x-www-form-urlencoded")
                .param("placeholderSequence", agentMessageId)

                .when()
                .post("/bot/fill")

                .then()
                .statusCode(200)
                .extract()
                .asString();
            messages.add(new MessageDto(agentMessageId, agentReply, response.placeHolderMessage().createdAt(), false));
        }
        messageContext.messages = messages;
    }

    private Response askResponse;

    @When("user ask chat bot {string}")
    public void userAskChatBot(String query) {

        MessagePlacedDto response =
            RestAssured.given()
                .headers("Authorization", "Bearer " + authContext.accessToken.accessToken())
                .param("message", query)

                .when()
                .post("/messages")

                .then()
                .body(not(emptyString()))
                .statusCode(200)
                .extract()
                .response().as(MessagePlacedDto.class);
        Long agentMessageId = response.placeHolderMessage().sequenceNumber();


        askResponse = RestAssured
            .given()
            .headers("Authorization", "Bearer " + authContext.accessToken.accessToken())
            .contentType("application/x-www-form-urlencoded")
            .param("placeholderSequence", agentMessageId)

            .when()
            .post("/bot/fill");
    }

    @Then("the chat bot answer something")
    public void theChatBotAnswerSomething() {
        askResponse.then()
            .body(not(emptyString()))
            .statusCode(200);
    }

    private Response messagesResponse;

    @When("he open message list")
    public void heOpenMessageList() {
        messagesResponse = RestAssured
            .given()
            .headers("Authorization", "Bearer " + authContext.accessToken.accessToken())
            .queryParam("anchorSeq", Long.MAX_VALUE - 1000)
            .when()
            .get("/messages");
    }

    @Then("the message list shows all of his {int} message")
    public void theMessageListShowsAllOfHisMessage(int count) {
        assertThat(messageContext.messages).hasSize(count);
        List<MessageDto> gotMessages = messagesResponse.then()
            .statusCode(200)
            .extract().as(new TypeRef<>() {
            });
        assertThat(gotMessages.reversed()).isEqualTo(messageContext.messages);
    }

    @When("he queries message before the {int} th message")
    public void heQueriesMessageBeforeTheThMessage(int offset) {
        MessageDto theMesage = messageContext.messages.get(offset - 1);

        messagesResponse = RestAssured
            .given()
            .headers("Authorization", "Bearer " + authContext.accessToken.accessToken())
            .queryParam("anchorSeq", theMesage.sequenceNumber())

            .when()
            .get("/messages");
    }

    @Then("the {int} messages before that message are returned")
    public void theMessagesBeforeThatMessageAreReturned(int count) {
        List<MessageDto> messages = messagesResponse.as(new TypeRef<>() {
        });
        assertThat(messages).hasSize(count);
        assertThat(messageContext.messages).hasSizeGreaterThanOrEqualTo(count);
        assertThat(messages.reversed()).isEqualTo(messageContext.messages.subList(0, count));
    }
}
