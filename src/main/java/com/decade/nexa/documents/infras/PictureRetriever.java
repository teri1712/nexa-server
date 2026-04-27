package com.decade.nexa.documents.infras;

import org.springframework.ai.rag.Query;

import java.util.List;

public interface PictureRetriever {
    List<Picture> retrieve(Query query);
}
