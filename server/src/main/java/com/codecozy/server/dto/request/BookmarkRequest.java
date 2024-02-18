package com.codecozy.server.dto.request;

public record BookmarkRequest(
    String date,
    int markPage,
    LocationRequest mainLocation,
    String uuid
) {}
