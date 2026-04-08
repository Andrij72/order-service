package com.akul.microservices.order.model;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * UserDetails.java.
 *
 * @author Andrii Kulynch
 * @version 1.0
 * @since 9/26/2025
 */
@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDetails {
    private String email;
    private String firstName;
    private String lastName;
}
