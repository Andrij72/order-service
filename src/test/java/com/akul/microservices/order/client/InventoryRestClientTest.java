package com.akul.microservices.order.client;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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
