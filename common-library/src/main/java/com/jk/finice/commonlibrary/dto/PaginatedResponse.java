package com.jk.finice.commonlibrary.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * Standard paginated response for all microservices
 * Wraps Spring Data Page into a consistent DTO
 *
 * @param <T> Type of content items
 * @author LastCoderBoy
 * @since 1.0.0
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PaginatedResponse<T> implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private List<T> content;
    private int totalPages;
    private long totalElements;
    private int currentPage;
    private int pageSize;

    /**
     * Constructor for simple list response without pagination metadata
     *
     * @param content list of items
     */
    public PaginatedResponse(List<T> content) {
        this.content = content;
        this.totalElements = content != null ? content.size() : 0;
        this.totalPages = content != null && !content.isEmpty() ? 1 : 0;
        this.currentPage = 0;
        this.pageSize = content != null ? content.size() : 0;
    }


    public static <T> PaginatedResponse<T> of(
            List<T> content,
            int totalPages,
            long totalElements,
            int currentPage,
            int pageSize
    ) {
        return new PaginatedResponse<>(
                content, totalPages, totalElements,
                currentPage, pageSize);
    }

    /**
     * Factory method for empty paginated response
     */
    public static <T> PaginatedResponse<T> empty() {
        return new PaginatedResponse<>(List.of());
    }
}


