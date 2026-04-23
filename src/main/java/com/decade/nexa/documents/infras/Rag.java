package com.decade.nexa.documents.infras;

import com.decade.nexa.documents.adapters.AiSuggestionService;
import com.decade.nexa.documents.application.ports.out.Ingestor;
import com.decade.nexa.documents.domain.DocType;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.document.Document;
import org.springframework.ai.model.transformer.KeywordMetadataEnricher;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Objects;

@Component
@RequiredArgsConstructor
public class Rag implements Ingestor, AiSuggestionService {

      private final VectorStore vectorStore;
      private final ChatModel chatModel;
      private final ChatClient chatClient;

      private TokenTextSplitter splitter() {
            return TokenTextSplitter.builder()
                      .withChunkSize(800)
                      .withMinChunkSizeChars(200)
                      .withMinChunkLengthToEmbed(5)
                      .withMaxNumChunks(5000)
                      .withKeepSeparator(true)
                      .build();
      }

      private KeywordMetadataEnricher enricher() {
            return new KeywordMetadataEnricher(chatModel, 5);
      }

      private List<Document> read(DocType type, Resource resource) {
            if (Objects.requireNonNull(type) == DocType.PDF) {
                  return new PagePdfDocumentReader(resource).read();
            }
            return new TikaDocumentReader(resource).read();
      }

      @Override
      public void ingest(DocType docType, Resource file) {
            List<Document> documents = read(docType, file);
            documents = splitter()
//                      .andThen(enricher())
                      .apply(documents);
            vectorStore.add(documents);
      }

      @Override
      public Flux<ChatResponse> suggest(Prompt prompt) {
            return chatClient.prompt(prompt)
                      .advisors(QuestionAnswerAdvisor.builder(vectorStore)
                                .searchRequest(SearchRequest.builder()
                                          .topK(5)
                                          .similarityThreshold(0.7)
                                          .build())
                                .build())
                      .stream().chatResponse();
      }

      @Override
      public ChatResponse suggestImmediately(Prompt prompt) {
            return chatClient.prompt(prompt)
                      .advisors(QuestionAnswerAdvisor.builder(vectorStore)
                                .searchRequest(SearchRequest.builder()
                                          .topK(5)
                                          .similarityThreshold(0.7)
                                          .build())
                                .build())
                      .call().chatResponse();
      }
}
