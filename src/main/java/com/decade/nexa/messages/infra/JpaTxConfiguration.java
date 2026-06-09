package com.decade.nexa.messages.infra;

import jakarta.persistence.EntityManagerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class JpaTxConfiguration {

    @Primary
    @Bean(name = "transactionManager")
    public PlatformTransactionManager jpaTxManager(EntityManagerFactory emf) {
        return new JpaTransactionManager(emf);
    }

}
