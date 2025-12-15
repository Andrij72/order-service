package com.akul.microservices.order.client;

import com.akul.microservices.order.exception.InventoryUnavailableException;

import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * InventoryClient.java.
 *
 * @author Andrii Kulynych
 * @since 9/2/2025
 */
public interface InventoryRestClient {

    Logger log = LoggerFactory.getLogger(InventoryRestClient.class);

    @CircuitBreaker(name = "inventoryServiceCB",
            fallbackMethod = "inventoryFallback")
    @Retry(name = "inventoryServiceCB")
    @Bulkhead(name = "inventoryServiceCB")
    @GetExchange("/api/v1/inventory")
    boolean isProductInStock(@RequestParam String sku,
                             @RequestParam Integer quantity);

    default boolean inventoryFallback(String sku,
                                      Integer quantity,
                                      Throwable throwable) {
        log.error(
                "*** Inventory service unavailable for sku={} quantity={}."
                + " Reason: {}",
                sku, quantity, throwable.getMessage()
        );
        throw new InventoryUnavailableException(
                "Inventory service is currently unavailable."
                + " Please try again later.");
    }
}
