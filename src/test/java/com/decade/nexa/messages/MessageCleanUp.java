package com.decade.nexa.messages;

import com.decade.nexa.common.DataCleanUp;
import com.decade.nexa.messages.application.ports.out.MessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.test.context.TestComponent;

@TestComponent
@RequiredArgsConstructor
public class MessageCleanUp implements DataCleanUp {
    private final MessageRepository messages;

    @Override
    public void clean() {
        messages.deleteAll();
    }
}
