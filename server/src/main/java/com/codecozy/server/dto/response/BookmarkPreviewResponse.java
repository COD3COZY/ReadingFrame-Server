package com.codecozy.server.dto.response;

public record BookmarkPreviewResponse(
        String date,
        int markPage,
        int markPercent,
        String location,
        String uuid
) {}
