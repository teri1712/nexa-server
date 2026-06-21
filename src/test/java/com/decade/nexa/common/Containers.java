package com.decade.nexa.common;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.DynamicPropertyRegistrar;
import org.testcontainers.containers.MinIOContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.elasticsearch.ElasticsearchContainer;

@Slf4j
@TestConfiguration
public class Containers {

    Network network = Network.newNetwork();

    @Bean
    @ServiceConnection
    PostgreSQLContainer<?> postgres() {
        return new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("mydatabase")
            .withExposedPorts(5432)
            .withUsername("myuser")
            .withPassword("secret")
            .withNetworkAliases("postgres")
            .withNetwork(network);
    }

    @Bean
    @ServiceConnection
    ElasticsearchContainer elasticsearch() {
        return new ElasticsearchContainer(
            "docker.elastic.co/elasticsearch/elasticsearch:8.17.0"
        )
            .withEnv("discovery.type", "single-node")
            .withEnv("xpack.security.enabled", "false");
    }
//
//    @ServiceConnection
//    @Bean
//    @Profile("ollama")
//    OllamaContainer ollama() {
//        OllamaContainer container = new OllamaContainer("ollama/ollama:0.6.6")
//            .withNetwork(network)
//            .withNetworkAliases("ollama")
//            .withFileSystemBind("/opt/.ollama", "/root/.ollama", BindMode.READ_WRITE)
//            .waitingFor(Wait.forHttp("/api/tags"))
//            .withExposedPorts(11434);
//        return container;
//    }
//
//    @Bean
//    LocalStackContainer localStackContainer() {
//        return new LocalStackContainer(DockerImageName.parse("localstack/localstack:3.0"))
//            .withServices(LocalStackContainer.Service.S3);
//    }

    @Bean
    MinIOContainer minioContainer() {
        return new MinIOContainer("minio/minio:RELEASE.2023-09-04T19-57-37Z")
            .withExposedPorts(9000)
            .withUserName("decadedecade")
            .withPassword("decadedecade");
    }


    @Bean
    DynamicPropertyRegistrar awsProperties(MinIOContainer minIO) {
        return registry -> {
            registry.add("aws.s3.endpoint", minIO::getS3URL);
            registry.add("aws.s3.bucket", () -> "test-bucket");
            registry.add("aws.s3.access.id", minIO::getUserName);
            registry.add("aws.s3.access.secret", minIO::getPassword);
        };
    }
//
//    @Bean
//    GenericContainer<?> faqSideCarContainer() {
//        return new GenericContainer<>("teri1712/faq_sidecar:latest")
//            .withNetwork(network)
//            .withLogConsumer(new Slf4jLogConsumer(log))
//            .withExposedPorts(8000)
//            .withCommand("python3", "-c", "print('started'); import time; time.sleep(3600)")
//            .waitingFor(org.testcontainers.containers.wait.strategy.Wait.forLogMessage(".*started.*\\n", 1))
//            .withEnv("DB_USER", "myuser")
//            .withEnv("DB_PASSWORD", "secret")
//            .withEnv("DB_HOST", "postgres")
//            .withEnv("DB_PORT", "5432")
//            .withEnv("DB_NAME", "mydatabase")
//            .withEnv("NUM_CLUSTERS", "3");
//    }

    @Bean(initMethod = "start", destroyMethod = "stop")
    public WireMockServer graphWireMockServer() {
        return new WireMockServer(WireMockConfiguration.wireMockConfig().dynamicPort());
    }

    @Bean(initMethod = "start", destroyMethod = "stop")
    public WireMockServer openAiWireMockServer() {
        return new WireMockServer(WireMockConfiguration.wireMockConfig().dynamicPort());
    }
//
//    @Bean
//    DynamicPropertyRegistrar faqSideCarProperties(GenericContainer<?> faqSideCarContainer) {
//        return registry -> {
//            registry.add("faq.sidecar.url", () -> "http://localhost:" + faqSideCarContainer.getMappedPort(8000));
//            log.info("faq.sidecar.url: {}", "http://localhost:" + faqSideCarContainer.getMappedPort(8000));
//        };
//    }


    @Bean
    DynamicPropertyRegistrar graphSideCarProperties(WireMockServer graphWireMockServer) {
        return registry -> {
            registry.add("graph.sidecar.url", () -> "http://localhost:" + graphWireMockServer.port());
            log.info("graph.sidecar.url: {}", "http://localhost:" + graphWireMockServer.port());
        };
    }

    @Bean
    DynamicPropertyRegistrar openAiProperties(WireMockServer openAiWireMockServer) {
        return registry -> {
            registry.add("spring.ai.openai.base-url", () -> "http://localhost:" + openAiWireMockServer.port());
            registry.add("spring.ai.openai.api-key", () -> "test-key");
            log.info("spring.ai.openai.base-url: {}", "http://localhost:" + openAiWireMockServer.port());
        };
    }


}
