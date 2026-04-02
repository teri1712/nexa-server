package com.decade.nexa.bdd.config;

import com.redis.testcontainers.RedisContainer;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.elasticsearch.ElasticsearchContainer;
import org.testcontainers.utility.DockerImageName;

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
      @ServiceConnection(name = "redis")
      RedisContainer redis() {
            return new RedisContainer(DockerImageName.parse("redis:6.2.6"))
                      .withExposedPorts(6379);
      }

      @Bean
      @ServiceConnection
      ElasticsearchContainer elasticsearch() {
            return new ElasticsearchContainer(
                      "docker.elastic.co/elasticsearch/elasticsearch:7.17.10"
            );
      }

}