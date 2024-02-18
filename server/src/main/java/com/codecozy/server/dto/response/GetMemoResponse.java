package com.codecozy.server.dto.response;

public record GetMemoResponse(
        String date,
        int markPage,
        String memoText,
        String uuid
) {}
