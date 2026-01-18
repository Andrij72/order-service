package com.akul.microservices.order.config;

import com.akul.microservices.order.client.InventoryRestClient;
import com.github.tomakehurst.wiremock.WireMockServer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

import static org.springframework.cloud.contract.wiremock.WireMockSpring.options;


@Configuration
@Profile("test")
public class TestRestClientConfig {

    @Bean
    public WireMockServer wireMockServer() {
        WireMockServer server = new WireMockServer(options().dynamicPort());
        server.start();
        return server;
    }

    @Bean
    public InventoryRestClient inventoryClient(WireMockServer wireMockServer, RestClient.Builder builder) {
        RestClient restClient = builder
                .baseUrl("http://localhost:" + wireMockServer.port())
                .build();

        HttpServiceProxyFactory factory = HttpServiceProxyFactory
                .builderFor(RestClientAdapter.create(restClient))
                .build();

        return factory.createClient(InventoryRestClient.class);
    }
}
