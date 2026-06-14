package com.decade.nexa.documents.integration;

import com.decade.nexa.common.TestDataset;
import com.decade.nexa.documents.application.ports.out.LogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.test.context.TestComponent;

@TestComponent
@RequiredArgsConstructor
public class IndexDataset implements TestDataset {

    final LogRepository logs;

    @Override
    public void clean() {
        logs.deleteAll();
    }
}
