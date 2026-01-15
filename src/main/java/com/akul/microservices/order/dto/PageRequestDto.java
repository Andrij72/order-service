package com.akul.microservices.order.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

/**
 * PageRequestDto.java.
 *
 * @author Andrii Kulynych
 * @since 1/15/2026
 */
public record PageRequestDto(

        @Min(0)
        Integer page,

        @Min(1)
        @Max(100)
        Integer size
) {

    private static final int DEFAULT_PAGE = 0;
    private static final int DEFAULT_SIZE = 20;

    public int pageOrDefault() {
        return page != null ? page : DEFAULT_PAGE;
    }

    public int sizeOrDefault() {
        return size != null ? size : DEFAULT_SIZE;
    }
}
