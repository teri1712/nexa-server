package com.decade.nexa.common;


import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.context.annotation.Bean;
import org.springframework.core.task.SyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;
import org.springframework.modulith.test.EnableScenarios;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestConstructor;

import java.util.List;

@SpringBootTest
@EnableScenarios
@AutoConfigureWireMock(port = 0)
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
@ActiveProfiles({"test", "ollama"})
@AutoConfigureMockMvc
public abstract class BaseTestClass {

    @Autowired
    List<TestDataset> datasets;

    @BeforeEach
    void setUp() {
        datasets.forEach(TestDataset::setup);
    }

    @AfterEach
    void cleanUp() {
        datasets.forEach(TestDataset::clean);
    }

    @TestConfiguration
    static class Config {
        @Bean
        public TaskExecutor taskExecutor() {
            return new SyncTaskExecutor();
        }
    }
}
