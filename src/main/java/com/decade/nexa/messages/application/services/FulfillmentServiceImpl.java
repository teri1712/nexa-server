package com.decade.nexa.messages.application.services;

import com.decade.nexa.messages.application.ports.in.FulfillmentService;
import com.decade.nexa.messages.application.ports.out.Agent;
import com.decade.nexa.messages.application.ports.out.BotMessageRepository;
import com.decade.nexa.messages.domain.AnswerMessage;
import com.decade.nexa.messages.domain.UserMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FulfillmentServiceImpl implements FulfillmentService {
    private final BotMessageRepository agentMessages;
    private final Agent agent;

    @Override
    public Flux<String> fill(UUID userId, Long placeholderSequence) {
        AnswerMessage answerMessage = agentMessages.findByUserIdAndSequenceId(userId, placeholderSequence)
            .orElseThrow();
        UserMessage userMessage = answerMessage.getUserMessage();
        StringBuilder sb = new StringBuilder();
        if (answerMessage.getContent() != null) {
            return Flux.just(answerMessage.getContent());
        }
        String question = userMessage.getContent();
        return agent.ask(answerMessage.getDocId(), userId, question)
            .doOnNext(sb::append)
            .doOnComplete(() -> {
                agentMessages.updateContent(placeholderSequence, sb.toString());
            });
    }
}
