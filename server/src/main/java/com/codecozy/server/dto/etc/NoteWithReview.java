package com.codecozy.server.dto.etc;

import com.codecozy.server.entity.BookReview;
import com.codecozy.server.entity.SelectReview;

// 리뷰 관련 뱃지 검사 시 사용
public record NoteWithReview(
        String keyword,
        SelectReview selectReview,
        BookReview bookReview
) {}
