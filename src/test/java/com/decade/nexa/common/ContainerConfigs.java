package com.decade.nexa.common;

import com.redis.testcontainers.RedisContainer;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.core.task.SyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;
import org.springframework.test.context.DynamicPropertyRegistrar;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.localstack.LocalStackContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.elasticsearch.ElasticsearchContainer;
import org.testcontainers.ollama.OllamaContainer;
import org.testcontainers.utility.DockerImageName;

import static org.testcontainers.containers.localstack.LocalStackContainer.Service.S3;

@TestConfiguration
public class ContainerConfigs {

      @Bean
      @ServiceConnection
      PostgreSQLContainer<?> postgres() {
            return new PostgreSQLContainer<>("postgres:16-alpine")
                      .withDatabaseName("mydatabase")
                      .withUsername("myuser")
                      .withPassword("secret");
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
                      .withEnv("xpack.security.enabled", "false")
                      .withEnv("ES_JAVA_OPTS", "-Xms512m -Xmx512m");
      }

      @ServiceConnection
      @Bean
      OllamaContainer ollama() {
            return new OllamaContainer("ollama/ollama:0.6.2")
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
      TaskExecutor taskExecutor() {
            return new SyncTaskExecutor();
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

}