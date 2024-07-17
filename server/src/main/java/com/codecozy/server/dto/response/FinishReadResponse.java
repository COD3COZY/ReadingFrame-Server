package com.codecozy.server.dto.response;

// 홈 - 다 읽은 책들 가져올 때 쓰는 DTO
public record FinishReadResponse(
        String isbn,
        String cover,
        String title,
        String author,
        int category,
        int bookType,
        Boolean isMine,
        Boolean isWriteReview
) {}