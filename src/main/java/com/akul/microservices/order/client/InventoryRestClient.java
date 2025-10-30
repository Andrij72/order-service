package com.akul.microservices.order.client;

import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;

/**
 * InventoryClient.java
 *
 * @author Andrii Kulynych
 * @since 9/2/2025
 */

public interface InventoryRestClient {

    @CircuitBreaker(name = "inventoryServiceCB", fallbackMethod = "inventoryFallback")
    @Retry(name = "inventoryServiceCB")
    @Bulkhead(name = "inventoryServiceCB")
    @GetExchange("/api/v1/inventory")
    boolean isProductInStock(@RequestParam String skuCode,
                             @RequestParam Integer quantity);
    }
