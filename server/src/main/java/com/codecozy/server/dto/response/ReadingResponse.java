package com.codecozy.server.dto.response;

// 홈 - 읽는 중인 책들 가져올 때 쓰는 DTO
public record ReadingResponse(
        String isbn,
        String cover,
        String title,
        String author,
        int readingPercent,
        int totalPage,
        int readPage,
        Boolean isHidden,
        int category,
        int bookType,
        Boolean isMine,
        Boolean isWriteReview
) {}
