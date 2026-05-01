package com.decade.nexa.documents;

import com.decade.nexa.common.BaseTestClass;
import com.decade.nexa.documents.infras.EntityExtractor;
import com.decade.nexa.files.apis.FileApi;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@Slf4j
@RequiredArgsConstructor
@ActiveProfiles({"test", "ollama", "graph-rag"})
class EntityExtracterTest extends BaseTestClass {

    @MockitoBean
    FileApi fileApi;

    final EntityExtractor entityExtractor;
    final DocumentCleanUp cleanUp;
    final Neo4JClean neo4jClean;

    @BeforeEach
    void clean() {
        cleanUp.clean();
        neo4jClean.clean();
    }

    @Test
    void shouldExtractCorrectlyFormat() {
        EntityExtractor.Extraction extraction = assertDoesNotThrow(() ->
            entityExtractor.extract("""
                    Paris is the capital and largest city of France.\s
                    Located in the north-central part of the country on the Seine River, it serves as the nation's primary hub for:\s
                
                        Politics: It is the seat of the French government, housing the president and the French Parliament.
                        Culture & Art: Home to world-renowned landmarks like the Eiffel Tower and the Louvre Museum.
                        Economy: Paris is the financial capital of France and a major global center for commerce, fashion, and gastronomy.
                        Administration: It is the central city of the Île-de-France region (the "Paris Region") and is itself one of the 101 departments of France.\s
                
                    Often called the "City of Light" (La Ville Lumière), it has been a major center of European education and the arts since the early Middle Ages.\s
                    Are you interested in planning a trip to Paris or learning about its historical landmarks?
                
                """)
        );

        assertThat(extraction.entities()).hasSizeGreaterThan(0);
        assertThat(extraction.relationships()).hasSizeGreaterThan(0);
    }
}
