package com.decade.nexa.messages.adapter;

import com.decade.nexa.messages.application.query.MessageService;
import com.decade.nexa.messages.dto.MessageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/messages")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;

    @GetMapping
    public List<MessageResponse> list(@RequestParam Long anchorSeq, @AuthenticationPrincipal(expression = "id") UUID userId) {
        return messageService.getMessages(userId, anchorSeq);
    }
}
