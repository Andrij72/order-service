package com.akul.microservices.order.domain.model;

import com.akul.microservices.order.domain.exception.BadRequestException;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "t_orders")
@Getter
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String orderNumber;

    @Version
    private Long version;

    @Embedded
    private UserDetails userDetails;

    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    @OneToMany(mappedBy = "order",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY)
    private List<OrderItem> items = new ArrayList<>();

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    @PrePersist
    void prePersist() {
        Instant now = Instant.now();
        if (createdAt == null) {
            createdAt = now;
        }
        if (updatedAt == null) {
            updatedAt = now;
        }
        if (status == null) {
            status = OrderStatus.PENDING;
        }
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = Instant.now();
    }


    public static Order create(String orderNumber, UserDetails user) {
        Order order = new Order();
        order.orderNumber = orderNumber;
        order.userDetails = user;
        order.status = OrderStatus.PENDING;
        return order;
    }

    public void addItem(OrderItem item) {
        item.setOrder(this);
        items.add(item);
    }

    public void updateUserDetails(UserDetails userDetails) {
        this.userDetails = userDetails;
    }

    public void markPaid() {
        if (!status.canTransitionTo(OrderStatus.PAID)) {
            throw new IllegalStateException(
                    "Cannot pay order in state " + status
            );
        }
        this.status = OrderStatus.PAID;
    }

    public void markFailed() {
        if (status.isTerminal()) {
            throw new IllegalStateException(
                    "Cannot fail order in terminal state " + status
            );
        }
        this.status = OrderStatus.FAILED;
    }

    public void markCancelled() {
        if (status.isTerminal()) {
            throw new IllegalStateException(
                    "Cannot cancel order in terminal state " + status
            );
        }
        this.status = OrderStatus.CANCELLED;
    }

    public void markDelivering() {
        if (!status.canTransitionTo(OrderStatus.DELIVERING)) {
            throw new IllegalStateException(
                    "Cannot move order to DELIVERING from " + status
            );
        }
        this.status = OrderStatus.DELIVERING;
    }

    public void markCompleted() {
        if (!status.canTransitionTo(OrderStatus.COMPLETED)) {
            throw new IllegalStateException(
                    "Cannot complete order from " + status
            );
        }
        this.status = OrderStatus.COMPLETED;
    }

    public void updateStatus(OrderStatus status) {

        validateTransition(status);

        switch (status) {

            case PAID -> markPaid();
            case FAILED -> markFailed();
            case CANCELLED -> markCancelled();
            case DELIVERING -> markDelivering();
            case COMPLETED -> markCompleted();

            default -> throw new BadRequestException("Unsupported status: " + status);
        }
    }

    public void cancel() {

        if (status == OrderStatus.PAID ||
            status == OrderStatus.COMPLETED) {

            throw new IllegalStateException("Cannot cancel paid/completed order");
        }

        this.status = OrderStatus.CANCELLED;
    }

    private void validateTransition(OrderStatus newStatus) {

        if (this.status == OrderStatus.COMPLETED) {
            throw new BadRequestException("Order already completed");
        }

        if (this.status == OrderStatus.CANCELLED) {
            throw new BadRequestException("Order already cancelled");
        }
    }

    public void markWaitingPayment() {

        if (!status.canTransitionTo(OrderStatus.WAITING_PAYMENT)) {
            throw new IllegalStateException(
                    "Cannot move order to WAITING_PAYMENT from " + status
            );
        }

        this.status = OrderStatus.WAITING_PAYMENT;
    }
}
