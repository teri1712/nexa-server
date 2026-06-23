package com.decade.nexa.documents.adapters;

import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.service.annotation.DeleteExchange;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.PostExchange;

public interface GraphSideCar {

    @PostExchange(value = "/index", contentType = MediaType.APPLICATION_JSON_VALUE)
        // return a uuid identifier
    void index(@RequestParam("requestId") Long requestId);

    @PostExchange(value = "/upload", contentType = MediaType.MULTIPART_FORM_DATA_VALUE)
    void upload(@RequestPart("file") Resource file);

    @DeleteExchange(value = "/files/{filename}", contentType = MediaType.MULTIPART_FORM_DATA_VALUE)
    void delete(String filename);

    @GetExchange("/local")
    String query(@RequestParam("query") String query);
}


