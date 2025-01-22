package com.codecozy.server.dto.request;

public record ModifyReadingStatusRequest(
        int readingStatus,
        String uuid
) {}
