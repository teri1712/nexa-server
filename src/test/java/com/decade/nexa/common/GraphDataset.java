package com.decade.nexa.common;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

public class GraphDataset implements TestDataset {

    @Override
    public void setup() {
        // POST /index
        stubFor(post(urlPathEqualTo("/index"))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "text/plain")
                .withBody("started")));

        // POST /upload
        stubFor(post(urlPathEqualTo("/upload"))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody("{\"status\": \"saved\"}")));

        // GET /index/{id}/progress
        stubFor(get(urlPathMatching("/index/[^/]+/progress"))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody("{\"status\": \"" + "completed" + "\", \"message\": \"done\"}")));

        // GET /drift (query method)
        stubFor(get(urlPathEqualTo("/drift"))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "text/plain")
                .withBody("hello world")));
    }

}
