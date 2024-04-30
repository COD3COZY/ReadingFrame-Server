package com.codecozy.server.dto.response;

public record GetMainBooksResponse(
        int readingStatus,
        String isbn,
        String cover,
        String title,
        String author,
        int readingPercent,
        int totalPage,
        int readPage,
        Boolean isMine
) {}
