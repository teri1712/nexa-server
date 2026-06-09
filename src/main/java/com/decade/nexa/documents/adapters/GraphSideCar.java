package com.decade.nexa.documents.adapters;

import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.PostExchange;

import java.util.Map;
import java.util.UUID;

public interface GraphSideCar {

    @PostExchange(value = "/index", contentType = MediaType.APPLICATION_JSON_VALUE)
        // return a uuid identifier
    void index(@RequestParam("requestId") Long requestId);

    @PostExchange(value = "/upload", contentType = MediaType.MULTIPART_FORM_DATA_VALUE)
    void upload(@RequestPart("file") Resource file);

    @GetExchange("/index/{id}/progress")
    Map<String, String> progress(@PathVariable("id") Long id);

    @GetExchange("/local")
        // return query
    String query(@RequestParam("query") String query);
}


