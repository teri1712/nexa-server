package com.decade.nexa.messages.adapter;

import com.decade.nexa.messages.application.ports.in.AgentService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.util.UUID;

@RestController
@RequestMapping("/agent")
@RequiredArgsConstructor
public class AgentController {

    private final AgentService agentService;

    @PostMapping("/ask")
    Flux<String> ask(@RequestParam String query, @AuthenticationPrincipal(expression = "id") UUID userId) {
        return agentService.ask(userId, query);
    }
}
