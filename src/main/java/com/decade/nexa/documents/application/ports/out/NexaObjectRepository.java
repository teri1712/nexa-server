package com.decade.nexa.documents.application.ports.out;

import com.decade.nexa.documents.domain.NexaObject;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;

import java.util.Collection;
import java.util.List;

public interface NexaObjectRepository extends Neo4jRepository<NexaObject, Long> {
    List<NexaObject> findByNameIsIn(Collection<String> names);

    @Query("""
        MATCH (src:nexa_object {name: $sourceName})
        MATCH(tgt:nexa_object {name: $targetName})
        MERGE (src)-[rel:HAS_RULE_TO {rule: $ruleText}]->(tgt)
        SET rel.description = $description
        """)
    void upsertRule(String sourceName, String targetName, String ruleText, String description);

    @Query("""
        MERGE (src:nexa_object {name: $sourceName, description: $description})
        """)
    void upsertNode(String name, String description);


    @Query("""
        MATCH (o: nexa_object)
        SET o.last_community = o.community
        """)
    void snapshotCluster();

    @Query("""
        CALL gds.graph.project('ragCommunity', 'nexa_object', 'HAS_RULE_TO') YIELD graphName
        CALL gds.leiden.write('ragCommunity', { writeProperty: 'community' }) YIELD communityCount
        CALL gds.graph.drop('ragCommunity') YIELD graphName AS dropped
        RETURN communityCount
        """)
    Long recluster();

    @Query("""
        MATCH (o:nexa_object)
        WHERE o.community IS NOT NULL
        AND (o.community <> o.last_community OR o.last_community IS NULL)
        RETURN DISTINCT o.community
        """)
    List<Long> dirtyOnes();

    List<NexaObject> findByCommunity(Long community);
}
