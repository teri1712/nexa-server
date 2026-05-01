package com.decade.nexa.common;

import org.springframework.boot.test.context.TestComponent;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.ComponentScan;

@TestConfiguration
@ComponentScan(includeFilters = @ComponentScan.Filter(TestComponent.class), basePackages = "com.decade.nexa")
public class DataCleanUpBeans {
}
