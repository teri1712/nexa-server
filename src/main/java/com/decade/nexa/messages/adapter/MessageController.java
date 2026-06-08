package com.decade.nexa.messages.adapter;

import com.decade.nexa.messages.application.ports.in.MessageService;
import com.decade.nexa.messages.dto.MessageDto;
import com.decade.nexa.messages.dto.MessagePlacedDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/doc-messages/{docId}")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class MessageController {

    private final MessageService messageService;

    @Operation(description = "Get message list whose sequence number are less than a sequence number", parameters = {
        @Parameter(name = "anchorSeq", description = "Anchor sequence number that ", required = true)
    },
        responses = {
            @ApiResponse(responseCode = "200", description = "max 20 messages in decreasing order"),
        }
    )
    @GetMapping
    public List<MessageDto> list(
        @PathVariable String docId,
        @RequestParam Long anchorSeq,
        @AuthenticationPrincipal(expression = "id") UUID userId) {
        return messageService.getMessages(docId, userId, anchorSeq);
    }

    @Operation(description = "Place a message to get agent response",
        parameters = @Parameter(name = "message", description = "Message to be placed", required = true),
        responses = {
            @ApiResponse(responseCode = "200",
                description = "Message placed successfully, return the placed message and an empty placeholder message used to ask bot to fill",
                content = @Content(mediaType = "application/json")),
        })
    @PostMapping
    public MessagePlacedDto post(
        @PathVariable String docId,
        @RequestParam String message,
        @AuthenticationPrincipal(expression = "id") UUID userId) {
        return messageService.addMessage(docId, userId, message);
    }
}
