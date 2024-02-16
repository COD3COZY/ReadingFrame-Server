package com.codecozy.server.dto.request;

import java.util.List;

public record ReviewCreateRequest (
    int readingStatus,
    int bookType,
    LocationCreateRequest mainLocation,
    boolean isMine,
    boolean isHidden,
    String startDate,
    String recentDate,
    BookCreateRequest bookInformation
) {}
