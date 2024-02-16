package com.codecozy.server.dto.request;

public record ReadingBookCreateRequest(
    int readingStatus,
    int bookType,
    LocationCreateRequest mainLocation,
    boolean isMine,
    boolean isHidden,
    String startDate,
    String recentDate,
    BookCreateRequest bookInformation
) {}
