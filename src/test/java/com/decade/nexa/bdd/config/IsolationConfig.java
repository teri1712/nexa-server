package com.decade.nexa.bdd.config;

import io.cucumber.java.Before;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;

@RequiredArgsConstructor
public class IsolationConfig {

      private final JdbcTemplate jdbcTemplate;
      private final StringRedisTemplate redisTemplate;

      @Before
      public void cleanUp() {
            jdbcTemplate.execute("truncate table user_member cascade");
            redisTemplate.getConnectionFactory().getConnection().serverCommands().flushAll();
      }
}
