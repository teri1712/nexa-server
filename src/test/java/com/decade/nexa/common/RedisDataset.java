package com.decade.nexa.common;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.test.context.TestComponent;

@RequiredArgsConstructor
@TestComponent
public class RedisDataset implements TestDataset {
//    private final StringRedisTemplate redisTemplate;
//
//    @Override
//    public void clean() {
//        redisTemplate.getConnectionFactory().getConnection().flushAll();
//    }
}
