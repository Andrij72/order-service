package com.akul.microservices.order.client;

import com.akul.microservices.order.exception.InventoryUnavailableException;
import io.github.resilience4j.bulkhead.BulkheadFullException;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class InventoryRestClientTest {

    private InventoryRestClient inventoryClient;

    @BeforeEach
    void setup() {

        inventoryClient = mock(InventoryRestClient.class);
    }


    @Test
    void testTimeout() {
        when(inventoryClient.isProductInStock("slow_sku", 1)).thenAnswer(invocation -> {
            Thread.sleep(1500);
            return true;
        });

        ExecutorService executor = Executors.newSingleThreadExecutor();
        Future<Boolean> future = executor.submit(() -> inventoryClient.isProductInStock("slow_sku", 1));

        assertThrows(TimeoutException.class, () -> future.get(1, TimeUnit.SECONDS));

        executor.shutdown();
    }
}
