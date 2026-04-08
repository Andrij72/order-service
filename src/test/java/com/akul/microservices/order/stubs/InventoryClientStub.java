package com.akul.microservices.order.stubs;

import com.github.tomakehurst.wiremock.WireMockServer;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

/**
 * InventoryClientStub.java
 *
 * @author Andrii Kulynch
 * @since 9/3/2025
 */
public class InventoryClientStub {
    public static void stubInventoryCall(String sku, int quantity, WireMockServer wireMockServer) {
        wireMockServer.stubFor(
                get(urlPathEqualTo("/api/v1/inventory"))
                        .withQueryParam("sku", equalTo(sku))
                        .withQueryParam("quantity", equalTo(String.valueOf(quantity)))
                        .willReturn(aResponse()
                                .withStatus(200)
                                .withHeader("Content-Type", "application/json")
                                .withBody("true")
                        )
        );
    }
}
