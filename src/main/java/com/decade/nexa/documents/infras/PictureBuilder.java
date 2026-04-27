package com.decade.nexa.documents.infras;

import org.springframework.ai.document.Document;

import java.util.List;

public interface PictureBuilder {
    void build(List<Document> docs);
}
