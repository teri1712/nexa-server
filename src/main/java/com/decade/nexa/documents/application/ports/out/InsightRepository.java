package com.decade.nexa.documents.application.ports.out;

import com.decade.nexa.documents.domain.DocumentInsight;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface InsightRepository extends ElasticsearchRepository<DocumentInsight, String> {
}
