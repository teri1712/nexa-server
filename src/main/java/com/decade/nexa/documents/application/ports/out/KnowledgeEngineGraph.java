package com.decade.nexa.documents.application.ports.out;

import java.util.UUID;

public interface KnowledgeEngineGraph extends KnowledgeEngine {

    void index(UUID requestId);

    boolean isFinished(UUID requestId);

}
