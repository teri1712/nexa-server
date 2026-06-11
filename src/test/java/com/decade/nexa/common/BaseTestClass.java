package com.decade.nexa.common;


import com.github.tomakehurst.wiremock.WireMockServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.core.task.SyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;
import org.springframework.modulith.test.EnableScenarios;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestConstructor;

import java.util.List;

@SpringBootTest
@EnableScenarios
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
@ActiveProfiles({"test", "openai"})
@AutoConfigureMockMvc
public abstract class BaseTestClass {

    @Autowired
    List<TestDataset> datasets;

    @Autowired(required = false)
    List<WireMockServer> wireMockServers;

    @BeforeEach
    void setUp() {
        datasets.forEach(TestDataset::setup);
    }

    @AfterEach
    void cleanUp() {
        datasets.forEach(TestDataset::clean);
        if (wireMockServers != null) {
            wireMockServers.forEach(WireMockServer::resetAll);
        }
    }

    @TestConfiguration
    static class Config {
        @Bean
        public TaskExecutor taskExecutor() {
            return new SyncTaskExecutor();
        }
    }
}
