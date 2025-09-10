package com.akul.microservices.order.stubs;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

/**
 * InventoryClientStub.java
 *
 * @author Andrii Kulynch
 * @since 9/3/2025
 */
public class InventoryClientStub {

    public static void stubInventoryCall(String sku, Integer quantity) {
        stubFor(get(urlEqualTo("/api/v1/inventory?skuCode=" + sku + "&quantity=" + quantity))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("true"))
        );
    }
}
