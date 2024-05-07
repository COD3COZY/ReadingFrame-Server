package com.codecozy.server.dto.response;

import java.util.List;

public record GetSearchBookResponse(
        String cover,
        String title,
        String author,
        String categoryName,
        int readingStatus,
        String publisher,
        String publicationDate,
        int totalPage,
        String description,
        int commentCount,
        List<Integer> selectReviewList,
        List<String> commentList
) {}
