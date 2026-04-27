package com.decade.nexa.documents.domain;

import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.RelationshipProperties;
import org.springframework.data.neo4j.core.schema.TargetNode;

@RelationshipProperties
public record NexaRule(
    @Id
    @GeneratedValue
    Long id,
    @TargetNode
    NexaObject target,
    String description,
    String rule
) {
}
