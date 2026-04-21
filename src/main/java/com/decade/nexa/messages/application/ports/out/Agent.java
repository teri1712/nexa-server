package com.decade.nexa.messages.application.ports.out;

import reactor.core.publisher.Flux;

import java.util.UUID;

public interface Agent {
    Flux<String> ask(UUID userId, String question);
}
