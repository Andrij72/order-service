package com.akul.microservices.order.controller;

import com.akul.microservices.order.dto.OrderRequest;
import com.akul.microservices.order.dto.OrderResponse;
import com.akul.microservices.order.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

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

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
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

    @GetMapping
    public ResponseEntity<List<OrderResponse>> getAllOrders() {
        List<OrderResponse> orders = orderService.getAllOrders();
        return ResponseEntity.ok(orders);
    }

    @DeleteMapping("/{orderNbr}")
    public ResponseEntity<String> deleteOrder(@PathVariable String orderNbr) {
                orderService.deleteOrder(orderNbr);
        return ResponseEntity.ok("Order with " +orderNbr + " successfully deleted ");
    }

    @PutMapping("/{orderNbr}")
    public ResponseEntity<OrderResponse> updateOrder(@PathVariable String orderNbr,
                                                     @RequestBody @Valid OrderRequest orderRequest) {
        OrderResponse response = orderService.update(orderNbr, orderRequest);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }
}
