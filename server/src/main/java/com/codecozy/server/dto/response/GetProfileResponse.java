package com.codecozy.server.dto.response;

public record GetProfileResponse(
        String nickName,
        int badgeCount,
        String profileImageCode
) {}
