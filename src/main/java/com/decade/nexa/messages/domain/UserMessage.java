package com.decade.nexa.messages.domain;

import jakarta.persistence.DiscriminatorValue;

@DiscriminatorValue("user")
public class UserMessage extends Message {
    
}
