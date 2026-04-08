package com.akul.microservices.order.config;

import com.akul.microservices.order.client.InventoryRestClient;
import io.micrometer.observation.ObservationRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

import java.time.Duration;

@Profile("!test")
@Configuration
@RequiredArgsConstructor
public class RestClientConfig {

    private final ObservationRegistry observationRegistry;

    @Value("${inventory.url}")
    private String inventoryUrl;

    @Bean
    @LoadBalanced
    public RestClient.Builder loadBalancedRestClientBuilder() {
        return RestClient.builder();
    }

    @Bean
    public InventoryRestClient inventoryClient(RestClient.Builder builder) {
        RestClient restClient = builder
                .baseUrl(inventoryUrl)
                .observationRegistry(observationRegistry)
                .requestFactory(getRequestFactory())
                .build();

        HttpServiceProxyFactory factory = HttpServiceProxyFactory
                .builderFor(RestClientAdapter.create(restClient))
                .build();

        return factory.createClient(InventoryRestClient.class);
    }

    private SimpleClientHttpRequestFactory getRequestFactory() {
        SimpleClientHttpRequestFactory factory =
                new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout((int) Duration.ofSeconds(3).toMillis());
        factory.setReadTimeout((int) Duration.ofSeconds(3).toMillis());
        return factory;
    }
}
