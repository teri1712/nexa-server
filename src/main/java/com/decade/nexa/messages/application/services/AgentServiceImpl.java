package com.decade.nexa.messages.application.services;

import com.decade.nexa.messages.application.ports.in.AgentService;
import com.decade.nexa.messages.application.ports.out.Agent;
import com.decade.nexa.messages.application.ports.out.MessageRepository;
import com.decade.nexa.messages.domain.AgentMessage;
import com.decade.nexa.messages.domain.UserMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AgentServiceImpl implements AgentService {
    private final MessageRepository messages;
    private final Agent agent;

    @Override
    public Flux<String> ask(UUID userId, String question) {
        UserMessage message = new UserMessage(question, userId);
        messages.save(message);
        StringBuilder sb = new StringBuilder();
        return agent.ask(userId, question)
            .doOnNext(sb::append)
            .doOnComplete(() -> {
                AgentMessage agentMessage = new AgentMessage(sb.toString(), userId);
                messages.save(agentMessage);
            });
    }
}
