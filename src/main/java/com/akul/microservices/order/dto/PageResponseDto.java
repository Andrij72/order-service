package com.akul.microservices.order.dto;

import org.springframework.data.domain.Page;

import java.util.List;

/**
 * PageResponse.java.
 *
 * @author Andrii Kulynych
 * @since 1/15/2026
 */
public record PageResponseDto<T>(
        List<T> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean last
) {
    public static <T> PageResponseDto<T> from(Page<T> page) {
        return new PageResponseDto<>(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isLast()
        );
    }
}
