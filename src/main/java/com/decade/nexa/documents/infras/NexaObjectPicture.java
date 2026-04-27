package com.decade.nexa.documents.infras;

import com.decade.nexa.documents.domain.NexaObject;

public record NexaObjectPicture(NexaObject nexaObject) implements Picture {
    @Override
    public String getDescription() {
        return nexaObject.name() + ": " + nexaObject.description();
    }
}
