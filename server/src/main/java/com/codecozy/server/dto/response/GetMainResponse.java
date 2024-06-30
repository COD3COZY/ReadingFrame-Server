package com.codecozy.server.dto.response;

import java.util.List;

public record GetMainResponse(
        List<GetMainBooksResponse> booksList,
        int wantToReadBooksCount,
        int readingBooksCount
) {}
