package com.decade.nexa.messages.application.ports.in;

import reactor.core.publisher.Flux;

import java.util.UUID;

public interface BotService {
    Flux<String> fill(UUID userId, Long placeholderSequence);
}
