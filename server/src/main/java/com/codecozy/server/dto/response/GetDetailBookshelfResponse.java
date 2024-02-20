package com.codecozy.server.dto.response;

public record GetDetailBookshelfResponse(
        String ISBN,
        String cover,
        String title,
        Integer bookType,
        int category,
        boolean isMine,
        int totalPage,
        int readPage,
        float readingPercent
) {}
