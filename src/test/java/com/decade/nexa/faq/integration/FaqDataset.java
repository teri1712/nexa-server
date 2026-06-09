package com.decade.nexa.faq.integration;

import com.decade.nexa.common.TestDataset;
import com.decade.nexa.faq.application.ports.out.ClusterLogRepository;
import com.decade.nexa.faq.application.ports.out.FAQRepository;
import com.decade.nexa.faq.application.ports.out.QueryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.test.context.TestComponent;

@TestComponent
@RequiredArgsConstructor
public class FaqDataset implements TestDataset {

    final FAQRepository faqs;
    final QueryRepository queries;
    final ClusterLogRepository logs;

    @Override
    public void clean() {
        faqs.deleteAll();
        queries.deleteAll();
        logs.deleteAll();
    }
}
