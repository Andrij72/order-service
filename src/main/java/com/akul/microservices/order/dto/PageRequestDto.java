package com.akul.microservices.order.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

import java.util.List;

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
        Integer size,

        List<String> sort
) {

    private static final int DEFAULT_PAGE = 0;
    private static final int DEFAULT_SIZE = 20;

    public static PageRequestDto of(Integer page, Integer size, List<String> sort) {
        int safePage = (page != null && page >= 0) ? page : DEFAULT_PAGE;
        int safeSize = (size != null && size > 0 && size <= 100) ? size : DEFAULT_SIZE;

        return new PageRequestDto(safePage, safeSize, sort);
    }

    public int pageOrDefault() {
        return page != null ? page : DEFAULT_PAGE;
    }

    public int sizeOrDefault() {
        return size != null ? size : DEFAULT_SIZE;
    }
}
