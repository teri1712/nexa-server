package com.decade.nexa.documents.domain;

import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Property;
import org.springframework.data.neo4j.core.schema.Relationship;

import java.util.List;

@Node("nexa_object")
public record NexaObject(
    @Id
    String name,
    String description,
    Long community,
    @Property("last_community")
    Long lastCommunity,
    @Relationship(type = "HAS_RULE_TO")
    List<NexaRule> rules
) {


}
