package com.akul.microservices.order.repository;


import com.akul.microservices.order.model.Order;
import com.akul.microservices.order.model.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * OrderRepository.java.
 *
 * @author Andrii Kulynch
 * @version 1.0
 * @since 8/22/2025
 */
public interface OrderRepository extends JpaRepository<Order, Long> {
    Optional<Order> findByOrderNumber(String orderNumber);

    Page<Order> findByStatus(
            OrderStatus status,
            Pageable pageable
    );

    Page<Order> findByUserDetailsEmail(
            String email,
            Pageable pageable
    );

    Page<Order> findByStatusAndUserDetailsEmail(
            OrderStatus status,
            String email,
            Pageable pageable
    );
}
