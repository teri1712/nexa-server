package com.decade.nexa.common;

import com.redis.testcontainers.RedisContainer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.test.context.DynamicPropertyRegistrar;
import org.testcontainers.containers.*;
import org.testcontainers.containers.localstack.LocalStackContainer;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.elasticsearch.ElasticsearchContainer;
import org.testcontainers.ollama.OllamaContainer;
import org.testcontainers.utility.DockerImageName;

import static org.testcontainers.containers.localstack.LocalStackContainer.Service.S3;

@Slf4j
@TestConfiguration
public class ContainerConfigs {

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
        return new OllamaContainer("ollama/ollama:0.6.6")
            .withFileSystemBind("/opt/.ollama", "/root/.ollama", BindMode.READ_WRITE)
            .waitingFor(Wait.forHttp("/api/tags"))
            .withExposedPorts(11434);
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
    @ServiceConnection
    Neo4jContainer<?> neo4jContainer() {
        return new Neo4jContainer<>("neo4j:5.15.0")
            .withFileSystemBind(System.getProperty("user.home") + "/neo4j-plugins", "/plugins", BindMode.READ_WRITE)
//            .withPlugins("graph-data-science", "apoc")
            .withoutAuthentication()
            .withNeo4jConfig("server.memory.heap.initial_size", "512M")
            .withNeo4jConfig("server.memory.heap.max_size", "1G")
            .withNeo4jConfig("server.memory.pagecache.size", "1G")
            .withEnv("NEO4J_ACCEPT_LICENSE_AGREEMENT", "yes");
    }


    @Bean
    GenericContainer<?> faqSideCar() {
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

    @Bean
    DynamicPropertyRegistrar faqSideCarProperties(GenericContainer<?> faqSideCar) {
        return registry -> {
            registry.add("faq.sidecar.url", () -> "http://localhost:" + faqSideCar.getMappedPort(8000));
            log.info("faq.sidecar.url: {}", "http://localhost:" + faqSideCar.getMappedPort(8000));
        };
    }

}