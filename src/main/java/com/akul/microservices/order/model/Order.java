package com.akul.microservices.order.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

/**
 * Order.java
 *
 * @author Andrii Kulynch
 * @version 1.0
 * @since 8/22/2025
 */

@Entity
@Table(name = "t_orders")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String orderNbr;
    @Column(length = 100)
    private String skuCode;
    private BigDecimal price;
    private Integer quantity;

    @Embedded
    private UserDetails userDetails;

}
