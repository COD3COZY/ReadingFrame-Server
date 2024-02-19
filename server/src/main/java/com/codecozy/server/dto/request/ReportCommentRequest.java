package com.codecozy.server.dto.request;

public record ReportCommentRequest(
        int reportType,
        String name
) {}
