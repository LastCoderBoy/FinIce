package com.jk.finice.commonlibrary.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

/**
 * Standard API response wrapper for all SIMS microservices
 * Provides a consistent response structure across all services
 *
 * @param <T> Type of data payload
 * @author LastCoderBoy
 * @since 2026-01-22
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ApiResponse<T> implements Serializable {

    private boolean success;
    private String message;
    private T data;

    public ApiResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
        this.data = null;
    }


//   ***** Factory methods for successful and error responses *****

    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(true, message, data);
    }

    public static <T> ApiResponse<T> success(String message) {
        return new ApiResponse<>(true, message);
    }

    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>(false, message);
    }

    public static <T> ApiResponse<T> error(String message, T data) {
        return new ApiResponse<>(false, message, data);
    }
}
