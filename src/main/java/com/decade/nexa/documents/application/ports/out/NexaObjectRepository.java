package com.decade.nexa.documents.application.ports.out;

import com.decade.nexa.documents.domain.NexaObject;
import io.micrometer.observation.annotation.Observed;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface NexaObjectRepository extends Neo4jRepository<NexaObject, String> {
    List<NexaObject> findByNameIsIn(Collection<String> names);

    @Query("""
        MATCH (n:nexa_object) WHERE n.name IN $names
        OPTIONAL MATCH (n)-[r1:HAS_RULE_TO]->(m1:nexa_object)
        OPTIONAL MATCH (m1)-[r2:HAS_RULE_TO]->(m2:nexa_object)
        RETURN n, collect(r1), collect(m1), collect(r2), collect(m2)
        """)
    List<NexaObject> findByNameIsInWithTwoHops(@Param("names") Collection<String> names);

    @Query("""
        MATCH (src:nexa_object {name: $sourceName})
        MATCH(tgt:nexa_object {name: $targetName})
        MERGE (src)-[rel:HAS_RULE_TO {rule: $ruleText}]->(tgt)
        SET rel.description = $description
        """)
    void upsertRule(String sourceName, String targetName, String ruleText, String description);

    @Query("""
         MERGE (src:nexa_object {name: $name})
         ON CREATE SET src.description = $description
         ON MATCH SET src.description = $description
        """)
    void upsertNode(String name, String description);

    @Query("""
         UNWIND $rows as row
         MERGE (src:nexa_object {name: row.name})
         ON CREATE SET src.description = row.description
         ON MATCH SET src.description = src.description + ". " + row.description
        """)
    void upsertNodes(@Param("rows") List<Map<String, String>> rows);


    @Query("""
        UNWIND $rules as rule
        MERGE (src:nexa_object {name: rule.sourceName})
        MERGE (tgt:nexa_object {name: rule.targetName})
        MERGE (src)-[rel:HAS_RULE_TO {rule: rule.ruleText}]->(tgt)
        ON CREATE SET rel.description = rule.description
        ON MATCH SET rel.description = coalesce(rel.description, '') + ". " + coalesce(rule.description, '')
        """)
    void upsertRules(@Param("rules") List<Map<String, String>> rules);


    @Observed(name = "snapshot.community")
    @Query("""
        MATCH (o: nexa_object)
        SET o.last_community = o.community
        """)
    void snapshotCluster();

    @Observed(name = "recluster.community")
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
