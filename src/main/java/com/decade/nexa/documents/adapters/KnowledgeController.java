package com.decade.nexa.documents.adapters;

import com.decade.nexa.documents.application.ports.out.KnowledgeEngine;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/knowledge")
@RequiredArgsConstructor
public class KnowledgeController {

    final KnowledgeEngine graph;

    @PostMapping(value = "/ask", produces = MediaType.TEXT_PLAIN_VALUE)
    String ask(@RequestParam String query) {
        return graph.ask(query);
    }

}
