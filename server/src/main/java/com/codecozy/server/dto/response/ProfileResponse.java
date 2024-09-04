package com.codecozy.server.dto.response;

public record ProfileResponse(
        String nickname,
        int badgeCount,
        String profileImageCode
) {}
