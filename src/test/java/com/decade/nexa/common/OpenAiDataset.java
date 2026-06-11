package com.decade.nexa.common;

import com.github.tomakehurst.wiremock.WireMockServer;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.TestComponent;

import java.util.Arrays;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

@TestComponent
public class OpenAiDataset implements TestDataset {

    private final WireMockServer openAiWireMockServer;

    public OpenAiDataset(@Qualifier("openAiWireMockServer") WireMockServer openAiWireMockServer) {
        this.openAiWireMockServer = openAiWireMockServer;
    }

    @Override
    public void clean() {
        openAiWireMockServer.resetAll();
    }

    @Override
    public void setup() {
        stubChatCompletion("Hello, how are you?");
        float[] embedding = new float[768];
        Arrays.fill(embedding, 0.1f);
        stubEmbeddings(embedding);
    }

    public void stubChatCompletion(String content) {
        // Non-streaming
        openAiWireMockServer.stubFor(post(urlEqualTo("/v1/chat/completions"))
            .withRequestBody(notMatching(".*\"stream\"\\s*:\\s*true.*"))
            .willReturn(aResponse()
                .withHeader("Content-Type", "application/json")
                .withBody("""
                    {
                      "id": "chatcmpl-123",
                      "object": "chat.completion",
                      "created": 1677652288,
                      "model": "gpt-4o",
                      "choices": [{
                        "index": 0,
                        "message": {
                          "role": "assistant",
                          "content": "%s"
                        },
                        "finish_reason": "stop"
                      }],
                      "usage": {
                        "prompt_tokens": 9,
                        "completion_tokens": 12,
                        "total_tokens": 21
                      }
                    }
                    """.formatted(content))));

        // Streaming
        openAiWireMockServer.stubFor(post(urlEqualTo("/v1/chat/completions"))
            .withRequestBody(matching(".*\"stream\"\\s*:\\s*true.*"))
            .willReturn(aResponse()
                .withHeader("Content-Type", "text/event-stream")
                .withBody("""
                    data: {"choices": [{"delta": {"content": "%s"}, "index": 0, "finish_reason": null}]}
                    data: {"choices": [{"delta": {}, "index": 0, "finish_reason": "stop"}]}
                    data: [DONE]
                    """.formatted(content))));
    }

    public void stubEmbeddings(float[] embedding) {
        StringBuilder embeddingArray = new StringBuilder("[");
        for (int i = 0; i < embedding.length; i++) {
            embeddingArray.append(embedding[i]);
            if (i < embedding.length - 1) {
                embeddingArray.append(",");
            }
        }
        embeddingArray.append("]");

        openAiWireMockServer.stubFor(post(urlEqualTo("/v1/embeddings"))
            .willReturn(aResponse()
                .withHeader("Content-Type", "application/json")
                .withBody("""
                    {
                      "object": "list",
                      "data": [
                        {
                          "object": "embedding",
                          "embedding": %s,
                          "index": 0
                        }
                      ],
                      "model": "text-embedding-3-small",
                      "usage": {
                        "prompt_tokens": 8,
                        "total_tokens": 8
                      }
                    }
                    """.formatted(embeddingArray.toString()))));
    }
}
