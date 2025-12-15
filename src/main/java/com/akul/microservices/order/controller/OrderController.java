package com.akul.microservices.order.controller;

import com.akul.microservices.order.dto.OrderRequest;
import com.akul.microservices.order.dto.OrderResponse;
import com.akul.microservices.order.dto.UpdateOrderStatusRequest;
import com.akul.microservices.order.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


/**
 * OderController.java.
 *
 * @author Andrii Kulynych
 * @version 1.0
 * @since 8/22/2025
 */
@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    // ---------------------------------------------------------------------
    // CREATE
    // ---------------------------------------------------------------------
    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(
            @RequestBody @Valid OrderRequest orderRequest) {
        OrderResponse response = orderService.placeOrder(orderRequest);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // ---------------------------------------------------------------------
    // GET ONE
    // ---------------------------------------------------------------------
    @GetMapping("/{orderNumber}")
    public ResponseEntity<OrderResponse> getOrder(
            @PathVariable String orderNumber) {

        return ResponseEntity.ok(
                orderService.getOrder(orderNumber)
        );
    }

    // ---------------------------------------------------------------------
    // GET ALL
    // ---------------------------------------------------------------------
    @GetMapping
    public ResponseEntity<List<OrderResponse>> getAllOrders() {

        return ResponseEntity.ok(
                orderService.getAllOrders()
        );
    }

    // ---------------------------------------------------------------------
    // UPDATE FULL ORDER
    // ---------------------------------------------------------------------
    @PutMapping("/{orderNumber}")
    public ResponseEntity<OrderResponse> updateOrder(
            @PathVariable String orderNumber,
            @RequestBody @Valid OrderRequest orderRequest) {

        OrderResponse response =
                orderService.update(orderNumber, orderRequest);

        return ResponseEntity.accepted().body(response);
    }

    // ---------------------------------------------------------------------
    // UPDATE STATUS
    // ---------------------------------------------------------------------
    @PatchMapping("/{orderNumber}/status")
    public ResponseEntity<OrderResponse> updateOrderStatus(
            @PathVariable String orderNumber,
            @RequestBody @Valid UpdateOrderStatusRequest request) {

        OrderResponse response =
                orderService.updateStatus(orderNumber, request.status());

        return ResponseEntity.ok(response);
    }

    // ---------------------------------------------------------------------
    // DELETE
    // ---------------------------------------------------------------------
    @DeleteMapping("/{orderNumber}")
    public ResponseEntity<Void> deleteOrder(
            @PathVariable String orderNumber) {

        orderService.deleteOrder(orderNumber);
        return ResponseEntity.noContent().build();
    }
}
