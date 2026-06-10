package com.decade.nexa.messages.integration;

import com.decade.nexa.common.TestDataset;
import com.decade.nexa.messages.application.ports.out.MessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.test.context.TestComponent;

@TestComponent
@RequiredArgsConstructor
public class MessageDataset implements TestDataset {
    private final MessageRepository messages;

    @Override
    public void setup() {
    }

    @Override
    public void clean() {
        messages.deleteAll();
    }
}
