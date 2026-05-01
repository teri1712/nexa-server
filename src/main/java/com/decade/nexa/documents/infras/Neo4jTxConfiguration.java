package com.decade.nexa.documents.infras;

import org.neo4j.driver.Driver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.neo4j.core.transaction.Neo4jTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class Neo4jTxConfiguration {

    @Bean(name = "neo4jtx")
    public PlatformTransactionManager neo4jTxManager(Driver driver) {
        return new Neo4jTransactionManager(driver);
    }
}
