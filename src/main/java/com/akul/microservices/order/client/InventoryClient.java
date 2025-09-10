package com.akul.microservices.order.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * InventoryClient.java
 *
 * @author Andrii Kulynch
 * @since 9/2/2025
 */

@FeignClient(name = "inventory-service", url = "${inventory.url}")
public interface InventoryClient {
    @RequestMapping(method = RequestMethod.GET, value = "/api/v1/inventory")
    boolean isProductInStock(@RequestParam String skuCode,
                             @RequestParam Integer quantity);
}
