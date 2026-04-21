package com.decade.nexa.messages.application.ports.in;

import reactor.core.publisher.Flux;

import java.util.UUID;

public interface AgentService {
    Flux<String> ask(UUID userId, Long placeholderSequence);
}
