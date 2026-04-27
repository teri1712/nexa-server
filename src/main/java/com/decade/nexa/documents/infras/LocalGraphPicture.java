package com.decade.nexa.documents.infras;

import com.decade.nexa.documents.application.ports.out.NexaObjectRepository;
import com.decade.nexa.documents.domain.NexaObject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.ai.document.Document;
import org.springframework.ai.rag.Query;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.FilterExpressionBuilder;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Stream;

@Slf4j
@Component
@RequiredArgsConstructor
@Profile("graph-rag")
public class LocalGraphPicture implements PictureRetriever, PictureBuilder, InitializingBean {

    private final VectorStore vectorStore;
    private final ChatClient.Builder builder;
    private final NexaObjectRepository nexaObjects;

    private ChatClient chatClient;

    @Override
    public void afterPropertiesSet() throws Exception {
        chatClient = builder.build();
    }

    @Override
    public List<Picture> retrieve(Query query) {
        SearchRequest request = SearchRequest.builder()
            .topK(5)
            .query(query.text())
            .filterExpression(new FilterExpressionBuilder().eq("graph_info", "nexa_entity").build())
            .build();
        List<String> entityNames = vectorStore.similaritySearch(request).stream().map(document -> document
            .getMetadata()
            .get("nexa_entity").toString()).toList();
        List<NexaObject> objects = nexaObjects.findByNameIsIn(entityNames);
        Stream<Picture> objectPictures = objects.stream().map(NexaObjectPicture::new);
        Stream<Picture> rulePictures = objects.stream().flatMap(new Function<NexaObject, Stream<Picture>>() {

            @Override
            public Stream<Picture> apply(NexaObject nexaObject) {
                return nexaObject.rules().stream().map(nexaRule -> new NexaRulePicture(nexaObject, nexaRule));
            }
        });
        return Stream.concat(objectPictures, rulePictures).toList();
    }

    public record ExtractionEntity(String name, String description) {
    }

    public record ExtractionRelationship(String source, String target, String relationship, String description) {
    }

    public record Extraction(List<ExtractionEntity> entities, List<ExtractionRelationship> relationships) {
    }

    public void extractAndSave(String rawDocumentText) {
        BeanOutputConverter<Extraction> converter =
            new BeanOutputConverter<>(Extraction.class);

        String persona = """
            You are an expert data extraction pipeline. Read the following text and extract all
            meaningful entities (people, organizations, concepts, tools) and the relationships between them.
            
            Text to analyze:
            {text}
            
            {format}
            """;

        try {
            Extraction extractedData = chatClient.prompt()
                .user(u -> u.text(persona)
                    .param("text", rawDocumentText)
                    .param("format", converter.getFormat()))
                .call()
                .entity(converter);

            if (extractedData == null || extractedData.entities().isEmpty()) {
                log.warn("No entities extracted from the text.");
                return;
            }

            log.info("Extracted {} entities and {} relationships.",
                extractedData.entities().size(), extractedData.relationships().size());
            extractedData.entities().forEach(entity -> {
                nexaObjects.upsertNode(entity.name(), entity.description());
            });
            List<Document> nexaObjectDocuments = extractedData.entities().stream().map(new Function<ExtractionEntity, Document>() {
                @Override
                public Document apply(ExtractionEntity object) {
                    return Document.builder()
                        .text(object.name() + ": " + object.description())
                        .metadata(Map.of("nexa_entity", object.name(), "graph_info", "nexa_entity"))
                        .build();
                }
            }).toList();
            vectorStore.add(nexaObjectDocuments);

            extractedData.relationships().forEach(relationship -> {
                nexaObjects.upsertRule(relationship.source(), relationship.target(), relationship.relationship(), relationship.description());
            });

            log.info("Document successfully ingested into Neo4j and Elasticsearch.");

        } catch (Exception e) {
            log.error("Failed to extract and ingest document: ", e);
        }
    }

    @Override
    public void build(List<Document> docs) {
        docs.forEach(doc -> extractAndSave(doc.getText()));
    }
}
