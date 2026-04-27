package com.decade.nexa.documents.infras;

import com.decade.nexa.documents.domain.NexaObject;
import com.decade.nexa.documents.domain.NexaRule;

public record NexaRulePicture(NexaObject nexaObject, NexaRule nexaRule) implements Picture {
    @Override
    public String getDescription() {
        return nexaObject.name() + " [" + nexaRule.rule() + "] " + nexaRule.target().name() + ": " + nexaRule.description();
    }
}
