package com.decade.nexa.documents.infras;

import org.springframework.ai.document.Document;

import java.util.concurrent.CompletableFuture;

// fuck aop
public interface ObjectGraphBuilder {
    CompletableFuture<Void> build(Document document);
}
