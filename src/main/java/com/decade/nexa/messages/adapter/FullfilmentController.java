package com.decade.nexa.messages.adapter;

import com.decade.nexa.messages.application.ports.in.FulfillmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class FullfilmentController {

    private final FulfillmentService fillService;

    @Operation(description = "Ask bot to answer",
        parameters = {
            @Parameter(name = "placeholderSequence", description = "Placeholder sequence number", required = true)
        },
        responses = {
            @ApiResponse(responseCode = "200", description = "Bot answer, return a text stream",
                content = @Content(mediaType = "text/plain"))
        })
    @PostMapping("/fill")
    Flux<String> fill(@RequestParam Long placeholderSequence, @AuthenticationPrincipal(expression = "id") UUID userId) {
        return fillService.fill(userId, placeholderSequence);
    }
}
