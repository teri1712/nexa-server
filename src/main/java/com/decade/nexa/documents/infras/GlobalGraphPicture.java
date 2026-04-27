package com.decade.nexa.documents.infras;

import com.decade.nexa.documents.application.ports.out.NexaObjectRepository;
import com.decade.nexa.documents.domain.NexaObject;
import lombok.RequiredArgsConstructor;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.ai.rag.Query;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.FilterExpressionBuilder;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Stream;

@Component
@RequiredArgsConstructor
@Profile("graph-rag")
public class GlobalGraphPicture implements PictureRetriever, InitializingBean {

    private final VectorStore vectorStore;
    private final ChatClient.Builder clientBuilder;
    private final NexaObjectRepository nexaObjects;
    private ChatClient client;


    @Override
    public void afterPropertiesSet() throws Exception {
        this.client = clientBuilder.build();
    }

    @Scheduled(cron = "0 0 2 * * *")
    @SchedulerLock(name = "global_picture_build", lockAtLeastFor = "1h", lockAtMostFor = "2h")
    void bulkBuild() {
        nexaObjects.snapshotCluster();
        nexaObjects.recluster();
        List<Long> dirties = nexaObjects.dirtyOnes();
        List<Document> documents = dirties.stream()
            .map(communityId -> {
                String summary = buildCommunitySummary(communityId);
                return Document.builder()
                    .id(communityId.toString())
                    .text(summary)
                    .metadata(Map.of("graph_info", "global_picture"))
                    .build();
            })
            .toList();
        vectorStore.add(documents);
    }

    private String buildCommunitySummary(Long communityId) {
        List<NexaObject> objects = nexaObjects.findByCommunity(communityId);
        Stream<Picture> objectPictures = objects.stream().map(NexaObjectPicture::new);
        Stream<Picture> rulePictures = objects.stream().flatMap(new Function<NexaObject, Stream<Picture>>() {
            @Override
            public Stream<Picture> apply(NexaObject nexaObject) {
                return nexaObject.rules().stream().map(nexaRule -> new NexaRulePicture(nexaObject, nexaRule));
            }
        });
        Stream<Picture> globalPictures = Stream.concat(objectPictures, rulePictures);
        String context = String.join("\n", globalPictures.map(Picture::getDescription).toList());
        String persona = "You are a an expert in summarzing documents. Please summarize the following text: " + context;

        return client.prompt().user(persona).call().content();
    }

    @Override
    public List<Picture> retrieve(Query query) {
        var filter = new FilterExpressionBuilder()
            .eq("graph_info", "global_picture").build();
        SearchRequest search = SearchRequest.builder()
            .topK(5)
            .filterExpression(filter)
            .query(query.text())
            .build();
        return vectorStore.similaritySearch(search).stream().map(new Function<Document, Picture>() {
            @Override
            public Picture apply(Document document) {
                return document::getText;
            }
        }).toList();
    }
}
