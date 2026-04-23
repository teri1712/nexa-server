package com.decade.nexa.documents.application;

import com.decade.nexa.documents.application.ports.in.SearchService;
import com.decade.nexa.documents.domain.Documentation;
import com.decade.nexa.documents.dto.DocFilter;
import com.decade.nexa.documents.dto.DocPage;
import com.decade.nexa.documents.dto.DocumentItemResponse;
import lombok.RequiredArgsConstructor;
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
public class SearchServiceImpl implements SearchService {

      private final ElasticsearchOperations es;

      @Override
      public DocPage search(DocFilter filter) {
            String query = filter.query();
            DateTimeFormatter formatter = DateTimeFormatter.ISO_INSTANT;
            NativeQueryBuilder buidler = NativeQuery.builder()
                      .withQuery(q -> q.bool(bool ->
                                bool
                                          .should(must -> must
                                                    .match(match -> match.field("filename").query(query).boost(2.0f))
                                          )
                                          .should(must -> must
                                                    .match(match -> match.field("title").query(query).boost(2.0f))
                                          )
                                          .should(must -> must
                                                    .match(match -> match.field("description").query(query))
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

}
