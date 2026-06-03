package com.decade.nexa.documents.application;

import com.decade.nexa.documents.application.ports.in.SearchService;
import com.decade.nexa.documents.application.ports.out.DocumentRepository;
import com.decade.nexa.documents.domain.Documentation;
import com.decade.nexa.documents.domain.events.UserSearched;
import com.decade.nexa.documents.dto.*;
import com.decade.nexa.files.apis.FileApi;
import com.decade.nexa.files.apis.FileIntegrityException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.client.elc.NativeQueryBuilder;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DocServiceImpl implements SearchService, DocService {

    private final ElasticsearchOperations es;
    private final FileApi fileApi;
    private final DocumentRepository docs;
    private final ApplicationEventPublisher publisher;


    @Override
    public DocPage search(DocFilter filter) {
        String query = filter.query();
        publisher.publishEvent(new UserSearched(filter.query()));

        DateTimeFormatter formatter = DateTimeFormatter.ISO_INSTANT;
        NativeQueryBuilder buidler = NativeQuery.builder()
            .withQuery(q -> q.bool(bool ->
                bool
                    .should(should -> should
                        .match(match -> match.field("title").query(query).boost(2.0f))
                    )
                    .should(should -> should
                        .match(match -> match.field("filename").query(query).boost(2.0f))
                    )
                    .should(should -> should
                        .match(match -> match.field("description").query(query).boost(1.0f))
                    )
                    .filter(must -> must
                        .term(term -> term.field("content_type").value(filter.type().name()))
                    )
                    .filter(must -> must
                        .range(r -> r
                            .date(date -> date.field("created_at")
                                .gte(formatter.format(filter.start()))
                                .lte(formatter.format(filter.end()))
                            ))
                    )
            ))
            .withSort(Sort.by("_score").descending().and(Sort.by("id").ascending()))
            .withPageable(Pageable.ofSize(10));
        if (filter.lastDoc() != null) {
            String lastId = filter.lastDoc().id();
            float lastScore = filter.lastDoc().score();
            buidler = buidler.withSearchAfter(List.of(lastId, lastScore));
        }
        SearchHits<Documentation> hits = es.search(buidler.build(), Documentation.class);
        List<DocumentItemResponse> docs = hits.stream().map(hit -> {
            Documentation documentation = hit.getContent();
            return DocumentItemResponse.builder()
                .id(documentation.getId())
                .title(documentation.getTitle())
                .filename(documentation.getFilename())
                .fileType(documentation.getContentType())
                .createdAt(documentation.getCreatedAt())
                .fileType(documentation.getContentType())
                .score(hit.getScore())
                .build();
        }).toList();
        return new DocPage((hits.getTotalHits() + 9) / 10, docs);
    }

    @Override
    public DocumentResponse add(CreateDocumentRequest request) throws FileIntegrityException {
        fileApi.getFile(request.fileKey(), request.eTag());
        Documentation documentation = new Documentation(request.fileKey(), request.filename(), request.title(), request.description(), request.type());
        docs.save(documentation);
        return DocumentResponse.builder()
            .id(documentation.getId())
            .title(documentation.getTitle())
            .filename(documentation.getFilename())
            .fileKey(documentation.getId())
            .description(documentation.getDescription())
            .fileType(documentation.getContentType())
            .createdAt(documentation.getCreatedAt())
            .build();
    }

    @Override
    public DocumentResponse find(String id) {
        return docs.findById(id).map(documentation ->
            DocumentResponse.builder()
                .id(documentation.getId())
                .title(documentation.getTitle())
                .filename(documentation.getFilename())
                .fileKey(documentation.getId())
                .description(documentation.getDescription())
                .fileType(documentation.getContentType())
                .createdAt(documentation.getCreatedAt())
                .build()).orElseThrow();
    }
}
