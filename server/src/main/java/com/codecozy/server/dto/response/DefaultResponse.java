package com.codecozy.server.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class DefaultResponse<T> {
    private int statusCode;
    private String message;
    private T data;

    public static <T> DefaultResponse<T> from(final int statusCode, final String message) {
        return from(statusCode, message, null);
    }

    public static <T> DefaultResponse<T> from(final int statusCode, final String message, final T data) {
        return DefaultResponse.<T>builder()
                .statusCode(statusCode)
                .message(message)
                .data(data)
                .build();
    }
}
