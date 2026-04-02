package com.decade.nexa.bdd.config;

import io.cucumber.java.Before;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;

@TestConfiguration
@RequiredArgsConstructor
public class IsolationConfig {

      private final JdbcTemplate jdbcTemplate;
      private final StringRedisTemplate redisTemplate;

      @Before
      void cleanUp() {
            jdbcTemplate.execute("truncate table user_member cascade");
            redisTemplate.getConnectionFactory().getConnection().flushAll();
      }
}
