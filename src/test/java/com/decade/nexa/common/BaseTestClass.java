package com.decade.nexa.common;


import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.core.task.SyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;
import org.springframework.modulith.test.EnableScenarios;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestConstructor;

@SpringBootTest
@EnableScenarios
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
@ActiveProfiles({"test", "ollama"})
@Import({ContainerConfigs.class, AIEvalutationConfig.class, DataCleanUpBeans.class})
@AutoConfigureMockMvc
public abstract class BaseTestClass {

    @TestConfiguration
    static class Config {
        @Bean
        public TaskExecutor taskExecutor() {
            return new SyncTaskExecutor();
        }
    }
}
