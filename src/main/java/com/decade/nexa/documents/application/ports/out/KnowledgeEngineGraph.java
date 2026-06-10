package com.decade.nexa.documents.application.ports.out;

public interface KnowledgeEngineGraph extends KnowledgeEngine {

    void index(Long requestId);

    boolean isFinished(Long requestId);

}
