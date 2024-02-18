package com.codecozy.server.dto.request;

public record MemoRequest(
        String uuid,
        String date,
        int markPage,
        String memoText
) {}
