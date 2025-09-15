package com.akul.microservices.order.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * OrderPlacedEvent.java
 *
 * @author Andrii Kulynch
 * @version 1.0
 * @since 9/11/2025
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderPlacedEvent {
    private String  orderNbr;
    private String  email;
}
