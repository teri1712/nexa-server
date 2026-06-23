package com.decade.nexa.common;

import com.decade.nexa.documents.application.ports.out.DocumentRepository;
import com.decade.nexa.documents.application.ports.out.InsightRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.test.context.TestComponent;
import org.springframework.data.elasticsearch.core.RefreshPolicy;

@RequiredArgsConstructor
@TestComponent
public class DocumentDataset implements TestDataset {

    final DocumentRepository docs;
    final InsightRepository insights;

    @Override
    public void clean() {
        docs.deleteAll(RefreshPolicy.IMMEDIATE);
        insights.deleteAll(RefreshPolicy.IMMEDIATE);
    }
}
