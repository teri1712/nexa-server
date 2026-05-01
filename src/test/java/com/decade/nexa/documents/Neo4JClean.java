package com.decade.nexa.documents;

import com.decade.nexa.common.DataCleanUp;
import com.decade.nexa.documents.application.ports.out.NexaObjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.test.context.TestComponent;

@TestComponent
@RequiredArgsConstructor
public class Neo4JClean implements DataCleanUp {

    private final NexaObjectRepository nexaObjects;

    @Override
    public void clean() {
        nexaObjects.deleteAll();
    }
}
