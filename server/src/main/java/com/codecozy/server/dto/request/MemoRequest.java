package com.codecozy.server.dto.request;

public record MemoRequest(
        String uuid,
        int markPage,
        String date,
        String memoText
) {}
