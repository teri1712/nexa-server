package com.decade.nexa.messages.application.ports.in;

import reactor.core.publisher.Flux;

import java.util.UUID;

public interface FulfillmentService {
    Flux<String> fill(UUID userId, Long placeholderSequence);
}
