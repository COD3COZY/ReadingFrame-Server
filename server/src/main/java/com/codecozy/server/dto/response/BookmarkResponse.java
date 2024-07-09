package com.codecozy.server.dto.response;

import java.util.List;

public record BookmarkResponse(
        String date,
        int markPage,
        int markPercent,
        List<String> location,
        String uuid
) {}
