package com.decade.nexa.documents.integration;

import com.decade.nexa.common.TestDataset;
import com.github.tomakehurst.wiremock.WireMockServer;
import org.springframework.beans.factory.annotation.Qualifier;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

public class GraphDataset implements TestDataset {

    private final WireMockServer graphWireMockServer;

    public GraphDataset(@Qualifier("graphWireMockServer") WireMockServer graphWireMockServer) {
        this.graphWireMockServer = graphWireMockServer;
    }

    @Override
    public void setup() {
        // POST /index
        graphWireMockServer.stubFor(post(urlPathEqualTo("/index"))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "text/plain")
                .withBody("started")));

        // POST /upload
        graphWireMockServer.stubFor(post(urlPathEqualTo("/upload"))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody("{\"status\": \"saved\"}")));

        // GET /index/{id}/progress
        graphWireMockServer.stubFor(get(urlPathMatching("/index/[^/]+/progress"))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody("{\"status\": \"" + "completed" + "\", \"message\": \"done\"}")));

        // GET /local (query method)
        graphWireMockServer.stubFor(get(urlPathEqualTo("/local"))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "text/plain")
                .withBody("hello world")));
    }

    @Override
    public void clean() {
        graphWireMockServer.resetAll();
    }
}
