package com.codecozy.server.dto.request;

import com.codecozy.server.entity.LocationList;

public record BookmarkRequest(
    String uuid,
    int markPage,
    LocationCreateRequest mainLocation,
    String date
) {}
