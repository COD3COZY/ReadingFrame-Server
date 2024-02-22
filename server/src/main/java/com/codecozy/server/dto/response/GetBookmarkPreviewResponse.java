package com.codecozy.server.dto.response;

public record GetBookmarkPreviewResponse(
        String date,
        int markPage,
        int markPercent,
        String location,
        String uuid
) {}
