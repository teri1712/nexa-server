package com.decade.nexa.documents.infras;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.document.Document;
import org.springframework.ai.rag.Query;
import org.springframework.ai.rag.retrieval.search.DocumentRetriever;
import org.springframework.ai.rag.retrieval.search.VectorStoreDocumentRetriever;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.function.Function;

@Component
@RequiredArgsConstructor
public class JustRag implements PictureRetriever, InitializingBean {

    private final VectorStore vectorStore;
    private DocumentRetriever documentRetriever;

    @Override
    public List<Picture> retrieve(Query query) {
        return documentRetriever.retrieve(query).stream().map(new Function<Document, Picture>() {
            @Override
            public Picture apply(Document document) {
                return new Picture() {
                    @Override
                    public String getDescription() {
                        return document.getText();
                    }
                };
            }
        }).toList();
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        documentRetriever = VectorStoreDocumentRetriever.builder().vectorStore(vectorStore)
            .topK(5)
            .similarityThreshold(0.7)
            .build();
    }
}
