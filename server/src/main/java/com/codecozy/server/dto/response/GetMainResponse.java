package com.codecozy.server.dto.response;

import java.util.List;

public record GetMainResponse(
        List<MainBooksResponse> booksList,
        int wantToReadBooksCount,
        int readingBooksCount
) {}
