package com.codecozy.server.dto.request;

public record CommentDetailRequest(
        int orderNumber,
        boolean orderType
) {}
