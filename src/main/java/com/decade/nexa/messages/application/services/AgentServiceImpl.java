package com.decade.nexa.messages.application.services;

import com.decade.nexa.messages.application.ports.in.AgentService;
import com.decade.nexa.messages.application.ports.out.Agent;
import com.decade.nexa.messages.application.ports.out.AgentMessageRepository;
import com.decade.nexa.messages.domain.AgentMessage;
import com.decade.nexa.messages.domain.UserMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AgentServiceImpl implements AgentService {
    private final AgentMessageRepository agentMessages;
    private final Agent agent;

    @Override
    public Flux<String> ask(UUID userId, Long placeholderSequence) {
        AgentMessage agentMessage = agentMessages.findByUserIdAndSequenceId(userId, placeholderSequence)
            .orElseThrow();
        UserMessage userMessage = agentMessage.getUserMessage();
        StringBuilder sb = new StringBuilder();
        if (agentMessage.getContent() != null) {
            return Flux.just(agentMessage.getContent());
        }
        String question = userMessage.getContent();
        return agent.ask(userId, question)
            .doOnNext(sb::append)
            .doOnComplete(() -> {
                agentMessages.updateContent(placeholderSequence, sb.toString());
            });
    }
}
