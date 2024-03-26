package com.codecozy.server.dto.response;

public record GetReadingResponse(
        String isbn,
        String cover,
        String title,
        String author,
        Double readingPercent,
        int totalPage,
        int readPage,
        Boolean isHidden,
        int category,
        int bookType,
        Boolean isMine,
        Boolean isWriteReview
) {}
