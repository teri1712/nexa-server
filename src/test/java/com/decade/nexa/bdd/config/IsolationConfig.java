package com.decade.nexa.bdd.config;

import io.cucumber.java.Before;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;

@RequiredArgsConstructor
public class IsolationConfig {

      private final JdbcTemplate jdbcTemplate;
      private final StringRedisTemplate redisTemplate;

      @Value("${super.admin.username}")
      private String superAdmin;

      @Before
      public void cleanUp() {
            jdbcTemplate.execute("delete from user_member where username != '" + superAdmin + "'");
            redisTemplate.getConnectionFactory().getConnection().serverCommands().flushAll();
      }
}
