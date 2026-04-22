package com.decade.nexa.messages.application.services;

import com.decade.nexa.messages.application.ports.in.BotService;
import com.decade.nexa.messages.application.ports.out.Agent;
import com.decade.nexa.messages.application.ports.out.BotMessageRepository;
import com.decade.nexa.messages.domain.BotMessage;
import com.decade.nexa.messages.domain.UserMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BotServiceImpl implements BotService {
    private final BotMessageRepository agentMessages;
    private final Agent agent;

    @Override
    public Flux<String> fill(UUID userId, Long placeholderSequence) {
        BotMessage botMessage = agentMessages.findByUserIdAndSequenceId(userId, placeholderSequence)
            .orElseThrow();
        UserMessage userMessage = botMessage.getUserMessage();
        StringBuilder sb = new StringBuilder();
        if (botMessage.getContent() != null) {
            return Flux.just(botMessage.getContent());
        }
        String question = userMessage.getContent();
        return agent.ask(userId, question)
            .doOnNext(sb::append)
            .doOnComplete(() -> {
                agentMessages.updateContent(placeholderSequence, sb.toString());
            });
    }
}
