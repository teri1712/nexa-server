package com.decade.nexa.common;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.redis.testcontainers.RedisContainer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.test.context.DynamicPropertyRegistrar;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.localstack.LocalStackContainer;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.elasticsearch.ElasticsearchContainer;
import org.testcontainers.ollama.OllamaContainer;
import org.testcontainers.utility.DockerImageName;

import static org.testcontainers.containers.localstack.LocalStackContainer.Service.S3;

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
    RedisContainer redis() {
        return new RedisContainer(DockerImageName.parse("redis:6.2.6"))
            .withExposedPorts(6379);
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

    @ServiceConnection
    @Bean
    @Profile("ollama")
    OllamaContainer ollama() {
        OllamaContainer container = new OllamaContainer("ollama/ollama:0.6.6")
            .withNetwork(network)
            .withNetworkAliases("ollama")
            .withFileSystemBind("/opt/.ollama", "/root/.ollama", BindMode.READ_WRITE)
            .waitingFor(Wait.forHttp("/api/tags"))
            .withExposedPorts(11434);
        return container;
    }

    @Bean
    LocalStackContainer localStackContainer() {
        return new LocalStackContainer(DockerImageName.parse("localstack/localstack:3.0"))
            .withServices(LocalStackContainer.Service.S3);
    }


    @Bean
    DynamicPropertyRegistrar awsProperties(LocalStackContainer localStack) {
        return registry -> {
            registry.add("aws.s3.endpoint", () -> localStack.getEndpointOverride(S3).toString());
            registry.add("aws.s3.access.id", localStack::getAccessKey);
            registry.add("aws.s3.access.secret", localStack::getSecretKey);
            registry.add("aws.s3.region", localStack::getRegion);
        };
    }

    @Bean
    GenericContainer<?> faqSideCarContainer() {
        return new GenericContainer<>("teri1712/faq_sidecar:latest")
            .withNetwork(network)
            .withLogConsumer(new Slf4jLogConsumer(log))
            .withExposedPorts(8000)
            .withEnv("DB_USER", "myuser")
            .withEnv("DB_PASSWORD", "secret")
            .withEnv("DB_HOST", "postgres")
            .withEnv("DB_PORT", "5432")
            .withEnv("DB_NAME", "mydatabase")
            .withEnv("NUM_CLUSTERS", "3");
    }

    @Bean(initMethod = "start", destroyMethod = "stop")
    public WireMockServer graphWireMockServer() {
        return new WireMockServer(WireMockConfiguration.wireMockConfig().dynamicPort());
    }

    @Bean
    DynamicPropertyRegistrar faqSideCarProperties(GenericContainer<?> faqSideCarContainer) {
        return registry -> {
            registry.add("faq.sidecar.url", () -> "http://localhost:" + faqSideCarContainer.getMappedPort(8000));
            log.info("faq.sidecar.url: {}", "http://localhost:" + faqSideCarContainer.getMappedPort(8000));
        };
    }


    @Bean
    DynamicPropertyRegistrar graphSideCarProperties(WireMockServer graphWireMockServer) {
        return registry -> {
            registry.add("graph.sidecar.url", () -> "http://localhost:" + graphWireMockServer.port());
            log.info("graph.sidecar.url: {}", "http://localhost:" + graphWireMockServer.port());
        };
    }


}
