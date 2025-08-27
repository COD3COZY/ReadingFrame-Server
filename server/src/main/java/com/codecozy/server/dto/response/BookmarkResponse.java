package com.codecozy.server.dto.response;


public record BookmarkResponse(
        String date,
        int markPage,
        int markPercent,
        BookmarkLocationInfoDto location,
        String uuid
) {}
