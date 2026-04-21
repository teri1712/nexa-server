package com.decade.nexa.bdd.context;

import com.decade.nexa.messages.dto.MessageDto;
import io.cucumber.spring.ScenarioScope;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@ScenarioScope
public class MessageContext {
    public List<MessageDto> messages;
}
