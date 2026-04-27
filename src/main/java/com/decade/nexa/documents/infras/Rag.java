package com.decade.nexa.documents.infras;

import com.decade.nexa.documents.application.ports.out.Ingestor;
import com.decade.nexa.documents.domain.DocType;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.document.Document;
import org.springframework.ai.rag.Query;
import org.springframework.ai.rag.advisor.RetrievalAugmentationAdvisor;
import org.springframework.ai.rag.retrieval.search.DocumentRetriever;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

@Component
public class Rag implements AI, Ingestor, InitializingBean {

    private final List<PictureRetriever> retrievers;
    private final List<PictureBuilder> pictureBuilders;
    private final ChatClient.Builder builder;
    private ChatClient chatClient;

    @Override
    public void ingest(DocType docType, List<Document> documents) {
        pictureBuilders.forEach(new Consumer<PictureBuilder>() {
            @Override
            public void accept(PictureBuilder pictureBuilder) {
                pictureBuilder.build(documents);
            }
        });
    }

    protected Rag(List<PictureRetriever> retrievers, List<PictureBuilder> pictureBuilders, ChatClient.Builder builder) {
        this.retrievers = retrievers;
        this.pictureBuilders = pictureBuilders;
        this.builder = builder;
    }


    @Override
    public void afterPropertiesSet() throws Exception {
        this.chatClient = builder
            .defaultAdvisors(RetrievalAugmentationAdvisor.builder()
                .documentRetriever(new DocumentRetriever() {
                    @Override
                    public List<Document> retrieve(Query query) {
                        return retrievers.stream().flatMap(new Function<PictureRetriever, Stream<Picture>>() {
                                @Override
                                public Stream<Picture> apply(PictureRetriever pictureRetriever) {
                                    return pictureRetriever.retrieve(query).stream();
                                }
                            })
                            .map(new Function<Picture, Document>() {

                                @Override
                                public Document apply(Picture picture) {
                                    return Document.builder().text(picture.getDescription()).build();
                                }
                            })
                            .toList();
                    }
                })
                .build())
            .build();
    }

    @Override
    public ChatClient.ChatClientRequestSpec suggest(Prompt prompt) {
        return chatClient.prompt(prompt);
    }
}
