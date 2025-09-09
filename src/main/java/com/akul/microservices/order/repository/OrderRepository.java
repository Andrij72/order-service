package com.akul.microservices.order.repository;


import com.akul.microservices.order.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * OrderRepository.java
 *
 * @author Andrii Kulynch
 * @version 1.0
 * @since 8/22/2025
 */
public interface OrderRepository extends JpaRepository<Order, Long> {
    Optional<Order> findByOrderNbr(String orderNbr);
}
