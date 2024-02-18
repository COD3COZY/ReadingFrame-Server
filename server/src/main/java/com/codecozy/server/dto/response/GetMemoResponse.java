package com.codecozy.server.dto.response;

public record GetMemoResponse(
        String date,
        int markPage,
        int markPercent,
        String memoText,
        String uuid
) {}
