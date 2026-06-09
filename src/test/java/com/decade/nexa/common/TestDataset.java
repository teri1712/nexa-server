package com.decade.nexa.common;

import org.springframework.boot.test.context.TestComponent;

@TestComponent
public interface TestDataset {

    default void clean() {
    }

    default void setup() {
    }
}
