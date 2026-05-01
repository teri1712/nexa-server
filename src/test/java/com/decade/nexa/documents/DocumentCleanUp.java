package com.decade.nexa.documents;

import com.decade.nexa.common.DataCleanUp;
import com.decade.nexa.documents.application.ports.out.DocumentRepository;
import com.decade.nexa.documents.application.ports.out.InsightRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.test.context.TestComponent;
import org.springframework.data.elasticsearch.core.RefreshPolicy;

@RequiredArgsConstructor
@TestComponent
public class DocumentCleanUp implements DataCleanUp {

    private final DocumentRepository docs;
    private final InsightRepository insights;

    @Override
    public void clean() {
        docs.deleteAll(RefreshPolicy.IMMEDIATE);
        insights.deleteAll(RefreshPolicy.IMMEDIATE);
    }
}
