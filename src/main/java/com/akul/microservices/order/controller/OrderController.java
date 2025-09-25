package com.akul.microservices.order.controller;

import com.akul.microservices.order.dto.OrderRequest;
import com.akul.microservices.order.dto.OrderResponse;
import com.akul.microservices.order.mappers.OrderMapper;
import com.akul.microservices.order.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * OderController.java
 *
 * @author Andrii Kulynch
 * @version 1.0
 * @since 8/22/2025
 */

@RestController
@RequestMapping("/api/v1/orders")
public class OrderController {

    private final OrderService orderService;
    private final OrderMapper orderMapper;

    public OrderController(OrderService orderService, OrderMapper orderMapper) {
        this.orderService = orderService;
        this.orderMapper = orderMapper;
    }

    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(@RequestBody @Valid OrderRequest orderRequest) {
        OrderResponse response = orderService.placeOrder(orderRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{orderNbr}")
    public ResponseEntity<OrderResponse> getOrder(@PathVariable String orderNbr) {
        OrderResponse response = orderService.getOrder(orderNbr);
        return ResponseEntity.ok(response);
    }
}