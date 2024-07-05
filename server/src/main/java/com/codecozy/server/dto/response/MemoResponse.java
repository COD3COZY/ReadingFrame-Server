package com.codecozy.server.dto.response;

public record MemoResponse(
        String date,
        int markPage,
        int markPercent,
        String memoText,
        String uuid
) {}
