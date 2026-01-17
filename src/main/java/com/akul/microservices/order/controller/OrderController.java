package com.akul.microservices.order.controller;

import com.akul.microservices.order.dto.OrderRequest;
import com.akul.microservices.order.dto.OrderResponse;
import com.akul.microservices.order.dto.PageRequestDto;
import com.akul.microservices.order.dto.PageResponseDto;
import com.akul.microservices.order.dto.UpdateOrderStatusRequest;
import com.akul.microservices.order.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
    public ResponseEntity<PageResponseDto<OrderResponse>> getAllOrders(
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) List<String> sort,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String email
    ) {
        PageRequestDto pageRequest = PageRequestDto.of(page, size, sort);
        Page<OrderResponse> result = orderService.getAllOrders(pageRequest, status, email);
        return ResponseEntity.ok(PageResponseDto.from(result));
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

    // ---------------------------------------------------------------------
    // UTIL
    // ---------------------------------------------------------------------
    private Sort.Order parseSort(String sort) {
        String[] parts = sort.split(",");
        return new Sort.Order(
                Sort.Direction.fromString(parts[1]),
                parts[0]
        );
    }
}
