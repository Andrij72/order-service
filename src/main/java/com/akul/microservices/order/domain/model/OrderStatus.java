package com.akul.microservices.order.domain.model;

public enum OrderStatus {

        PENDING,
        WAITING_PAYMENT,
        PAID,
        DELIVERING,
        COMPLETED,
        CANCELLED,
        FAILED;

        public boolean isTerminal() {
            return this == COMPLETED ||
                   this == CANCELLED ||
                   this == FAILED;
        }

        public boolean canTransitionTo(OrderStatus newStatus) {

            return switch (this) {

                case PENDING ->
                        newStatus == WAITING_PAYMENT ||
                        newStatus == CANCELLED ||
                        newStatus == FAILED;

                case WAITING_PAYMENT ->
                        newStatus == PAID ||
                        newStatus == CANCELLED ||
                        newStatus == FAILED;

                case PAID ->
                        newStatus == DELIVERING ||
                        newStatus == FAILED;

                case DELIVERING ->
                        newStatus == COMPLETED ||
                        newStatus == FAILED;

                case COMPLETED, CANCELLED, FAILED ->
                        false;
            };
        }
    }
