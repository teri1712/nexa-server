package com.decade.nexa.documents.application.ports.out;

import com.decade.nexa.documents.domain.Documentation;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DocumentRepository extends ElasticsearchRepository<Documentation, String> {
}
