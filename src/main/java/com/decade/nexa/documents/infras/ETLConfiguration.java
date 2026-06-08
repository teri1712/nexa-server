package com.decade.nexa.documents.infras;

import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ETLConfiguration {

    @Bean
    TokenTextSplitter splitter() {
        return TokenTextSplitter.builder()
            .withChunkSize(800)
            .withMinChunkSizeChars(200)
            .withMinChunkLengthToEmbed(5)
            .withMaxNumChunks(5000)
            .withKeepSeparator(true)
            .build();
    }

}
