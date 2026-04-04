package com.decade.nexa.bdd.config;

import io.cucumber.java.Before;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;

@RequiredArgsConstructor
public class IsolationConfig {

      private final JdbcTemplate jdbcTemplate;
      private final StringRedisTemplate redisTemplate;
      private final S3Client s3Client;
      private @Value("${aws.s3.bucket}") String bucket;

      @Before
      public void setUpBucket() {
            s3Client.createBucket(CreateBucketRequest.builder()
                      .bucket(bucket)
                      .build());
      }

      @Before
      public void cleanUp() {
            jdbcTemplate.execute("truncate table user_member, upload_record cascade");
            redisTemplate.getConnectionFactory().getConnection().serverCommands().flushAll();
      }
}
