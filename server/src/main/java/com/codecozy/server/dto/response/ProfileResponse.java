package com.codecozy.server.dto.response;

public record ProfileResponse(
        String nickName,
        int badgeCount,
        String profileImageCode
) {}
