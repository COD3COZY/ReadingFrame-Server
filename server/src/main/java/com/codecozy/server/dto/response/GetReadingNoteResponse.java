package com.codecozy.server.dto.response;

import java.util.List;

public record GetReadingNoteResponse(
        String cover,
        String title,
        String author,
        String categoryName,
        int totalPage,
        int readPage,
        int readingPercent,
        String firstReviewDate,
        String keywordReview,
        String commentReview,
        List<Integer> selectReview,
        boolean isMine,
        int bookType,
        int readingStatus,
        String mainLocation,
        String startDate,
        String recentDate,
        List<BookmarkPreviewResponse> bookmarks,
        List<MemoResponse> memos,
        List<PersonalDictionaryPreviewResponse> characters
) {}
