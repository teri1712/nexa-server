package com.decade.nexa.documents.infras;

import io.micrometer.observation.annotation.Observed;

import java.util.List;

public interface EntityExtractor {

    record ExtractionEntity(String name, String description) {
    }

    record ExtractionRelationship(String source, String target, String relationship, String description) {
    }

    record Extraction(List<ExtractionEntity> entities, List<ExtractionRelationship> relationships) {
    }


    @Observed(name = "extract.entity", contextualName = "entity-rule-extraction")
    Extraction extract(String context);
}
