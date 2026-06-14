package com.decade.nexa.documents.infras;

import org.springframework.ai.document.DocumentTransformer;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

@Configuration
public class ETLConfiguration {

    @Bean
    @Order(1)
    DocumentTransformer splitter() {
        return TokenTextSplitter.builder()
            .withChunkSize(800)
            .withMinChunkSizeChars(200)
            .withMinChunkLengthToEmbed(5)
            .withMaxNumChunks(5000)
            .withKeepSeparator(true)
            .build();
    }

}
