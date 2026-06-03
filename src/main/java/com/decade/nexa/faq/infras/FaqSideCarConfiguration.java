package com.decade.nexa.faq.infras;

import com.decade.nexa.faq.adapters.FaqSideCar;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

@Configuration
public class FaqSideCarConfiguration {
    @Bean
    FaqSideCar sideCar(@Value("${faq.sidecar.url}") String url) {

        RestClient restClient = RestClient.builder()
            .baseUrl(url)
            .build();

        HttpServiceProxyFactory factory =
            HttpServiceProxyFactory
                .builderFor(RestClientAdapter.create(restClient))
                .build();

        return factory.createClient(FaqSideCar.class);
    }
}
