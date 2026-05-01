package com.decade.nexa.documents.infras;

import com.decade.nexa.documents.application.ports.out.NexaObjectRepository;
import com.decade.nexa.documents.domain.NexaObject;
import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationRegistry;
import io.micrometer.observation.annotation.Observed;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.data.neo4j.core.transaction.Neo4jTransactionManager;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

@Slf4j
@RequiredArgsConstructor
@Component
public class LLMBasedObjectGraphBuilder implements ObjectGraphBuilder, InitializingBean {

    private final VectorStore vectorStore;
    private final NexaObjectRepository nexaObjects;
    private final Neo4jTransactionManager neo4jTxManager;
    private final ObservationRegistry observationRegistry;
    private final EntityExtractor entityExtractor;
    private TransactionTemplate txTemplate;

    @Override
    public void afterPropertiesSet() throws Exception {
        txTemplate = new TransactionTemplate(neo4jTxManager);
    }


    @Observed(name = "extract_and_save_document")
    @Async
    public CompletableFuture<Void> build(Document document) {
        String rawDocumentText = document.getText();
        Observation observation = observationRegistry.getCurrentObservation();
        if (observation != null)
            observation.highCardinalityKeyValue("document_id", document.getId());

        try {
            EntityExtractor.Extraction extractedData = entityExtractor.extract(rawDocumentText);

            if (extractedData == null || extractedData.entities().isEmpty()) {
                log.warn("No entities extracted from the text.");
                return CompletableFuture.completedFuture(null);
            }
            txTemplate.executeWithoutResult(status -> {
                nexaObjects.upsertNodes(extractedData.entities()
                    .stream().map(extractionEntity ->
                        Map.of(
                            "name", extractionEntity.name(),
                            "description", extractionEntity.description()))
                    .toList()
                );
                nexaObjects.upsertRules(extractedData.relationships().stream().map(extractionRelationship ->
                        Map.of(
                            "sourceName", extractionRelationship.source(),
                            "targetName", extractionRelationship.target(),
                            "ruleText", extractionRelationship.relationship(),
                            "description", extractionRelationship.description()))
                    .toList());
            });
            List<NexaObject> updatedOnes = nexaObjects.findByNameIsIn(extractedData.entities().stream().map(EntityExtractor.ExtractionEntity::name).toList());
            List<Document> nexaObjectDocuments = updatedOnes.stream().map(new Function<NexaObject, Document>() {
                @Override
                public Document apply(NexaObject object) {
                    return Document.builder()
                        .text(object.name() + ": " + object.description())
                        .id("nexa_object_" + object.name())
                        .metadata(Map.of("nexa_entity", object.name(), "graph_info", "nexa_entity"))
                        .build();
                }
            }).toList();
            vectorStore.add(nexaObjectDocuments);

            log.info("Document successfully ingested into Neo4j and Elasticsearch.");

            return CompletableFuture.completedFuture(null);
        } catch (Exception e) {
            log.error("Failed to extract and ingest document: ", e);
            return CompletableFuture.failedFuture(e);
        }
    }

}
