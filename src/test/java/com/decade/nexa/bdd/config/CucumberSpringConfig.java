package com.decade.nexa.bdd.config;

import com.decade.nexa.common.ContainerConfigs;
import com.decade.nexa.common.DataCleanUpBeans;
import com.decade.nexa.common.OIDCConfig;
import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@CucumberContextConfiguration
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Import({ContainerConfigs.class, OIDCConfig.class, DataCleanUpBeans.class})
public class CucumberSpringConfig {
}
