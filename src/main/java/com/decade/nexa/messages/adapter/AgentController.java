package com.decade.nexa.messages.adapter;

import com.decade.nexa.messages.application.ports.in.AgentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
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

    @Operation(description = "Ask agent to answer",
        parameters = {
            @Parameter(name = "placeholderSequence", description = "Placeholder sequence number", required = true)
        }
        , responses = {
        @ApiResponse(responseCode = "200", description = "Agent answer, return a text stream",
            content = @Content(mediaType = "text/plain"))
    })
    @PostMapping("/ask")
    Flux<String> ask(@RequestParam Long placeholderSequence, @AuthenticationPrincipal(expression = "id") UUID userId) {
        return agentService.ask(userId, placeholderSequence);
    }
}
