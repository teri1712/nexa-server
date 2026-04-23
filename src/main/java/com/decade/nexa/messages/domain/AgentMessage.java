package com.decade.nexa.messages.domain;

import jakarta.persistence.DiscriminatorValue;

@DiscriminatorValue("agent")
public class AgentMessage extends Message {
}
