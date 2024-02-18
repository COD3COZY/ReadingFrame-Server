package com.codecozy.server.dto.request;

public record BookmarkRequest(
    String uuid,
    int markPage,
    LocationRequest mainLocation,
    String date
) {}
