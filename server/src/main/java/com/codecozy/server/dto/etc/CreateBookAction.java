package com.codecozy.server.dto.etc;

import com.codecozy.server.entity.Book;

// 뱃지 검사 시 사용
public record CreateBookAction(
        int readingStatus,
        Book book
) {}
