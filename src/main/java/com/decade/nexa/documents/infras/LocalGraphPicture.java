package com.decade.nexa.documents.infras;

import com.decade.nexa.documents.application.ports.out.NexaObjectRepository;
import com.decade.nexa.documents.domain.NexaObject;
import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationRegistry;
import io.micrometer.observation.annotation.Observed;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.rag.Query;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.FilterExpressionBuilder;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Stream;

@Slf4j
@Component
@RequiredArgsConstructor
@Profile("graph-rag")
@Qualifier("local-graph")
public class LocalGraphPicture implements PictureRetriever, PictureBuilder {

    private final VectorStore vectorStore;
    private final NexaObjectRepository nexaObjects;
    private final ObservationRegistry observationRegistry;
    private final ObjectGraphBuilder objectGraphBuilder;

    @Override
    public List<Picture> retrieve(Query query) {
        SearchRequest request = SearchRequest.builder()
            .topK(5)
            .query(query.text())
            .filterExpression(new FilterExpressionBuilder().eq("graph_info", "nexa_entity").build())
            .build();
        List<String> entityNames = vectorStore.similaritySearch(request).stream().map(document -> document
                .getMetadata()
                .get("nexa_entity").toString())
            .distinct()
            .toList();
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

    @Value("${spring.threads.virtual.enabled}")
    private boolean virtual;


    @Override
    @Observed(name = "local.graph.builder")
    public CompletableFuture<Void> build(List<Document> docs) {
        Observation observation = observationRegistry.getCurrentObservation();
        if (observation != null)
            observation.lowCardinalityKeyValue("thread", virtual ? "virtual" : "platform");


        return CompletableFuture.allOf(docs.stream()
            .map(objectGraphBuilder::build)
            .toArray(CompletableFuture[]::new));
    }

}
