package com.codecozy.server.dto.request;

public record ReadingBookCreateRequest(
    int readingStatus,
    int bookType,
    LocationRequest mainLocation,
    boolean isMine,
    String startDate,
    String recentDate,
    BookCreateRequest bookInformation
) {}
