package com.akul.microservices.order.client;

import com.akul.microservices.order.exception.InventoryUnavailableException;
import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;

/**
 * InventoryClient.java
 *
 * @author Andrii Kulynych
 * @since 9/2/2025
 */
public interface InventoryRestClient {

    Logger log = LoggerFactory.getLogger(InventoryRestClient.class);

    @CircuitBreaker(name = "inventoryServiceCB", fallbackMethod = "inventoryFallback")
    @Retry(name = "inventoryServiceCB")
    @Bulkhead(name = "inventoryServiceCB")
    @GetExchange("/api/v1/inventory")
    boolean isProductInStock(@RequestParam String skuCode,
                             @RequestParam Integer quantity);

    default boolean inventoryFallback(String skuCode, Integer quantity, Throwable throwable) {
        log.error("*** Inventory service unavailable for skuCode={} quantity={}. Reason: {}",
                skuCode, quantity, throwable.getMessage());
        throw new InventoryUnavailableException("Inventory service is currently unavailable. Please try again later.");
    }
}
